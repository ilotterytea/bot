pub mod api;
pub(super) mod schema;

use common::{
    establish_connection,
    models::ChannelFeature,
    schema::{channel_preferences::dsl as chp, channels::dsl as ch},
};
use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};
use futures::SinkExt;
use log::info;
use serde_json::Value;
use std::{collections::HashSet, str::FromStr, sync::Arc, time::Duration};

use eyre::Context;
use reqwest::Url;
use tokio::net::TcpStream;
use tokio_tungstenite::{
    connect_async_with_config,
    tungstenite::{protocol::WebSocketConfig, Message},
    MaybeTlsStream, WebSocketStream,
};
use twitch_api::types::UserId;

use crate::{
    instance_bundle::InstanceBundle, localization::LineId, seventv::schema::Payload,
    shared_variables::SEVENTV_WEBSOCKET_URL,
};

use self::schema::*;

async fn connect(url: Url) -> Result<WebSocketStream<MaybeTlsStream<TcpStream>>, eyre::Error> {
    let config = WebSocketConfig::default();

    let (socket, _) = connect_async_with_config(url, Some(config), false).await?;

    Ok(socket)
}

pub struct SevenTVWebsocketClient {
    socket: WebSocketStream<MaybeTlsStream<TcpStream>>,
    session_id: Option<String>,
    instance_bundle: Arc<InstanceBundle>,
    reconnect_url: Url,

    listening_channel_ids: HashSet<UserId>,
}

impl SevenTVWebsocketClient {
    pub async fn new(instance_bundle: Arc<InstanceBundle>) -> Result<Self, eyre::Error> {
        let reconnect_url = Url::parse(SEVENTV_WEBSOCKET_URL).unwrap();

        Ok(Self {
            instance_bundle,
            socket: connect(reconnect_url.clone()).await?,
            session_id: None,
            reconnect_url,
            listening_channel_ids: HashSet::new(),
        })
    }

    pub async fn run(&mut self) -> Result<(), eyre::Error> {
        loop {
            self.process_awaiting_channels().await?;
            tokio::select!(
                Some(msg) = futures::StreamExt::next(&mut self.socket) => {
                    let msg = match msg {
                        Err(tungstenite::Error::Protocol(tungstenite::error::ProtocolError::ResetWithoutClosingHandshake)) => {
                            self.socket = connect(self.reconnect_url.clone()).await.context("when reestablishing connection")?;
                            continue
                        }
                        _ => msg.context("when getting message")?,
                    };
                    self.process_message(msg).await?
                }
            )
        }
    }

    async fn flip_channels(&mut self) {
        let mut data = self.instance_bundle.seventv_eventapi_data.lock().await;

        let mut set_a: HashSet<UserId> = HashSet::from_iter(data.iter().cloned());
        let set_b: HashSet<UserId> = HashSet::from_iter(self.listening_channel_ids.iter().cloned());

        set_a.extend(set_b);

        *data = HashSet::from_iter(set_a.into_iter());
        self.listening_channel_ids.clear();
    }

    pub async fn process_message(&mut self, msg: Message) -> Result<(), eyre::Report> {
        match msg {
            Message::Text(s) => {
                println!("received 7tv text message: {s}");

                if let Ok(e) = serde_json::from_str::<Payload<Value>>(s.as_str()) {
                    let d = e.d.to_string();
                    let d = d.as_str();

                    match e.op {
                        // Dispatch
                        0 => {
                            if let Ok(d) = serde_json::from_str::<Dispatch>(d) {
                                self.handle_dispatch(d).await?;
                            }
                        }
                        // Hello
                        1 => {
                            if let Ok(d) = serde_json::from_str::<Hello>(d) {
                                if self.session_id.is_none() {
                                    self.session_id = Some(d.session_id);
                                    self.process_awaiting_channels().await?;
                                } else {
                                    self.resume_session().await?;
                                }
                            }
                        }
                        // Heartbeat
                        2 => println!("[7TV EventAPI] Heartbeat!"),
                        // Reconnect
                        4 => {
                            println!("[7TV EventAPI] Reconnect!");

                            self.flip_channels().await;
                            self.socket.close(None).await?;
                            self.socket = connect(self.reconnect_url.clone()).await?;
                        }
                        // Error
                        6 => println!("[7TV EventAPI] Error: {}", e.d),
                        // End of Stream
                        7 => {
                            println!("[7TV EventAPI] The host has closed the connection! Reason: ")
                        }
                        _ => println!(
                            "[7TV EventAPI] Unhandled opcode: {}. Payload: {}",
                            e.op, e.d
                        ),
                    }
                }
            }
            Message::Close(e) => {
                let e = if e.is_some() {
                    let unwrapped_e = e.unwrap();

                    format!("{} {}", unwrapped_e.code, unwrapped_e.reason)
                } else {
                    "No reason".to_string()
                };

                info!(
                    "The connection to 7TV EventAPI has been disconnected. Reason: {}",
                    e
                );

                self.flip_channels().await;

                let mut attempts = 0;

                loop {
                    if attempts > 2 {
                        break;
                    }

                    tokio::time::sleep(Duration::from_secs(60)).await;

                    match connect(self.reconnect_url.clone()).await {
                        Ok(v) => self.socket = v,
                        Err(_) => {
                            attempts += 1;
                            info!("Failed to reconnect to 7TV EventAPI! {} attempts left.", {
                                3 - attempts
                            });
                        }
                    }
                }
            }
            _ => {}
        }

        Ok(())
    }

    async fn handle_dispatch(&mut self, body: Dispatch) -> Result<(), eyre::Error> {
        if body.event_type != *"emote_set.update" {
            println!("[7TV EventAPI] Unhandled body type: {}", body.event_type);
            return Ok(());
        }

        let api = self.instance_bundle.seventv_api_client.clone();

        if let Some(emote_set) = api.get_emote_set(body.body.id).await {
            if let Some(emote_set_owner) = emote_set.owner {
                if let Some(emote_set_owner) = api.get_user(emote_set_owner.id).await {
                    if let Some(owner) = emote_set_owner
                        .connections
                        .iter()
                        .find(|x| x.platform.eq("TWITCH"))
                    {
                        let actor_name = if let Some(connection) = body
                            .body
                            .actor
                            .connections
                            .iter()
                            .find(|x| x.platform.eq("TWITCH"))
                        {
                            connection.username.clone()
                        } else {
                            body.body.actor.username
                        };

                        let conn = &mut establish_connection();

                        let owner_id = owner.id.parse::<i32>().unwrap();

                        let channel = ch::channels
                            .filter(ch::alias_id.eq(&owner_id))
                            .select((ch::id, ch::alias_name))
                            .get_result::<(i32, String)>(conn)
                            .expect("Failed to get channel");

                        let (channel_language, channel_features) = chp::channel_preferences
                            .filter(chp::channel_id.eq(&channel.0))
                            .select((chp::language, chp::features))
                            .get_result::<(String, Vec<Option<String>>)>(conn)
                            .expect("Failed to get channel preference");

                        if !channel_features.iter().flatten().any(|x| {
                            if let Ok(f) = ChannelFeature::from_str(x.as_str()) {
                                f == ChannelFeature::Notify7TVUpdates
                            } else {
                                false
                            }
                        }) {
                            return Ok(());
                        }

                        let mut messages: Vec<String> = Vec::new();

                        if let Some(pushed) = body.body.pushed {
                            for e in pushed {
                                let emote_name = e.value.unwrap().name;

                                messages.push(
                                    self.instance_bundle
                                        .localizator
                                        .get_formatted_text(
                                            channel_language.as_str(),
                                            LineId::EmotesPushed,
                                            vec![
                                                self.instance_bundle
                                                    .localizator
                                                    .get_literal_text(
                                                        channel_language.as_str(),
                                                        LineId::Provider7TV,
                                                    )
                                                    .unwrap(),
                                                actor_name.clone(),
                                                emote_name,
                                            ],
                                        )
                                        .unwrap(),
                                );
                            }
                        }

                        if let Some(pulled) = body.body.pulled {
                            for e in pulled {
                                let emote_name = e.old_value.unwrap().name;

                                messages.push(
                                    self.instance_bundle
                                        .localizator
                                        .get_formatted_text(
                                            channel_language.as_str(),
                                            LineId::EmotesPulled,
                                            vec![
                                                self.instance_bundle
                                                    .localizator
                                                    .get_literal_text(
                                                        channel_language.as_str(),
                                                        LineId::Provider7TV,
                                                    )
                                                    .unwrap(),
                                                actor_name.clone(),
                                                emote_name,
                                            ],
                                        )
                                        .unwrap(),
                                )
                            }
                        }

                        if let Some(updated) = body.body.updated {
                            for e in updated {
                                let emote_name = e.value.unwrap().name;
                                let old_emote_name = e.old_value.unwrap().name;

                                messages.push(
                                    self.instance_bundle
                                        .localizator
                                        .get_formatted_text(
                                            channel_language.as_str(),
                                            LineId::EmotesUpdated,
                                            vec![
                                                self.instance_bundle
                                                    .localizator
                                                    .get_literal_text(
                                                        channel_language.as_str(),
                                                        LineId::Provider7TV,
                                                    )
                                                    .unwrap(),
                                                actor_name.clone(),
                                                old_emote_name,
                                                emote_name,
                                            ],
                                        )
                                        .unwrap(),
                                )
                            }
                        }

                        for m in messages {
                            self.instance_bundle
                                .twitch_irc_client
                                .say(channel.1.clone(), m)
                                .await
                                .expect("Failed to send a message");
                        }
                    }
                }
            }
        }

        Ok(())
    }

    async fn process_awaiting_channels(&mut self) -> Result<(), eyre::Error> {
        let mut data = self.instance_bundle.seventv_eventapi_data.lock().await;

        let ids = data
            .iter()
            .filter(|x| !self.listening_channel_ids.iter().any(|y| y.eq(*x)))
            .cloned()
            .collect::<HashSet<UserId>>();

        *data = ids.clone();

        drop(data);

        if !ids.is_empty() {
            for id in ids {
                self.listen_channel(id).await?;
            }
        }

        Ok(())
    }

    async fn listen_channel(&mut self, channel_id: UserId) -> Result<(), eyre::Error> {
        if let Some(user) = self
            .instance_bundle
            .seventv_api_client
            .get_user_by_twitch_id(channel_id.clone().take())
            .await
        {
            let emote_set_id = user.emote_set.id;

            let data = Payload {
                op: 35,
                d: Subscribe {
                    event_type: "emote_set.update".to_string(),
                    condition: SubscribeCondition {
                        object_id: emote_set_id,
                    },
                },
            };

            println!("{:?}", serde_json::to_string(&data).unwrap());
            self.socket
                .send(Message::Text(serde_json::to_string(&data).unwrap()))
                .await?;

            println!("Listening 7TV events for {}'s emote set", user.username);

            self.listening_channel_ids.insert(channel_id);
        }

        Ok(())
    }

    async fn resume_session(&mut self) -> Result<(), eyre::Error> {
        if self.session_id.is_none() {
            println!("[7TV EventAPI] Failed to resume a session because session_id is none!");

            return Ok(());
        }

        let data = Payload {
            op: 34,
            d: Resume {
                session_id: self.session_id.clone().unwrap(),
            },
        };

        self.socket
            .send(Message::Text(serde_json::to_string(&data)?))
            .await?;

        Ok(())
    }
}
