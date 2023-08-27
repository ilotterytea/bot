use std::sync::Arc;

use crate::{
    locale::{LineId, Localizations},
    models::diesel::Channel,
    schema::channels::dsl as ch,
    shared_variables::SEVENTV_WEBSOCKET_URL,
};
use diesel::RunQueryDsl;
use eyre::Context;
use futures_util::SinkExt;
use serde_json::Value;
use tokio::net::TcpStream;
use tokio_tungstenite::{
    connect_async_with_config, tungstenite,
    tungstenite::{protocol::WebSocketConfig, Message},
    MaybeTlsStream, WebSocketStream,
};
use twitch_api::{twitch_oauth2::UserToken, types::UserId, HelixClient};
use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

use crate::{
    seventv::schemes::{Dispatch, Hello, Payload},
    utils::establish_connection,
};

use super::{
    api::SevenTVAPIClient,
    schemes::{Resume, Subscribe, SubscribeCondition},
};

pub struct SevenTVWebsocketClient {
    irc_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    seventv_api_client: Arc<SevenTVAPIClient>,
    waiting_channel_ids: Vec<String>,
    listening_channel_ids: Vec<String>,
    session_id: Option<String>,
    connect_url: url::Url,
}

impl SevenTVWebsocketClient {
    pub fn new(
        irc_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
        seventv_api_client: Arc<SevenTVAPIClient>,
    ) -> Self {
        Self {
            irc_client,
            seventv_api_client,
            waiting_channel_ids: Vec::new(),
            listening_channel_ids: Vec::new(),
            session_id: None,
            connect_url: url::Url::parse(SEVENTV_WEBSOCKET_URL).unwrap(),
        }
    }

    pub async fn connect(&self) -> Result<WebSocketStream<MaybeTlsStream<TcpStream>>, eyre::Error> {
        let config = WebSocketConfig {
            max_send_queue: None,
            max_message_size: Some(64 << 20),
            max_frame_size: Some(16 << 20),
            accept_unmasked_frames: false,
        };

        let (socket, _) = connect_async_with_config(&self.connect_url, Some(config), false).await?;

        Ok(socket)
    }

    pub async fn run(mut self) -> Result<(), eyre::Error> {
        let mut s = self
            .connect()
            .await
            .context("when establishing connection")?;

        loop {
            self.listen_channels_from_waiting_list(&mut s).await?;
            tokio::select!(
                Some(msg) = futures::StreamExt::next(&mut s) => {
                    let msg = match msg {
                        Err(tungstenite::Error::Protocol(tungstenite::error::ProtocolError::ResetWithoutClosingHandshake)) => {
                            s = self.connect().await.context("when reestablishing connection")?;
                            continue
                        }
                        _ => msg.context("when getting message")?,
                    };
                    self.process_message(&mut s, msg).await?
                }
            )
        }
    }

    pub async fn process_message(
        &mut self,
        socket: &mut WebSocketStream<MaybeTlsStream<TcpStream>>,
        msg: Message,
    ) -> Result<(), eyre::Report> {
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
                                    self.listen_channels(socket).await?;
                                } else {
                                    self.resume_session(socket).await?;
                                }
                            }
                        }
                        // Heartbeat
                        2 => println!("[7TV EventAPI] Heartbeat!"),
                        // Reconnect
                        4 => println!("[7TV EventAPI] Reconnect!"),
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

                println!("The connection to 7TV EventAPI was refused: {e}");
            }
            _ => {}
        }

        Ok(())
    }

    async fn handle_dispatch(&mut self, body: Dispatch) -> Result<(), eyre::Error> {
        if body.event_type != "emote_set.update".to_string() {
            println!("[7TV EventAPI] Unhandled body type: {}", body.event_type);
            return Ok(());
        }

        if let Some(emote_set) = self.seventv_api_client.get_emote_set(body.body.id).await {
            if let Some(emote_set_owner) = emote_set.owner {
                if let Some(emote_set_owner) =
                    self.seventv_api_client.get_user(emote_set_owner.id).await
                {
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

                        let mut messages: Vec<String> = Vec::new();

                        if let Some(pushed) = body.body.pushed {
                            for e in pushed {
                                let emote_name = e.value.unwrap().name;

                                messages.push(
                                    Localizations::formatted_text(
                                        "english",
                                        LineId::EMOTES_PUSHED,
                                        vec![
                                            Localizations::literal_text(
                                                "english",
                                                LineId::PROVIDERS_SEVENTV,
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
                                    Localizations::formatted_text(
                                        "english",
                                        LineId::EMOTES_PULLED,
                                        vec![
                                            Localizations::literal_text(
                                                "english",
                                                LineId::PROVIDERS_SEVENTV,
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
                                    Localizations::formatted_text(
                                        "english",
                                        LineId::EMOTES_UPDATE,
                                        vec![
                                            Localizations::literal_text(
                                                "english",
                                                LineId::PROVIDERS_SEVENTV,
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
                            self.irc_client
                                .say(owner.username.clone(), m)
                                .await
                                .expect("Failed to send a message");
                        }
                    }
                }
            }
        }

        Ok(())
    }

    async fn listen_channels_from_waiting_list(
        &mut self,
        socket: &mut WebSocketStream<MaybeTlsStream<TcpStream>>,
    ) -> Result<(), eyre::Error> {
        if !self.waiting_channel_ids.is_empty() {
            for channel_id in self.waiting_channel_ids.clone() {
                self.listen_channel(socket, channel_id).await?;
            }

            self.waiting_channel_ids.clear();
        }

        Ok(())
    }

    pub async fn put_channel_on_waiting_list(&mut self, channel_id: String) -> bool {
        if self.listening_channel_ids.contains(&channel_id) {
            println!("channel id {} is already listening", channel_id);

            return false;
        }

        self.waiting_channel_ids.push(channel_id);
        true
    }

    async fn listen_channels(
        &mut self,
        socket: &mut WebSocketStream<MaybeTlsStream<TcpStream>>,
    ) -> Result<(), eyre::Error> {
        let conn = &mut establish_connection();

        if let Ok(channels) = ch::channels.load::<Channel>(conn) {
            for channel in channels {
                self.listen_channel(socket, channel.alias_id.to_string())
                    .await?;
            }
        }

        Ok(())
    }

    pub async fn listen_channel(
        &mut self,
        socket: &mut WebSocketStream<MaybeTlsStream<TcpStream>>,
        channel_id: String,
    ) -> Result<(), eyre::Error> {
        if self.listening_channel_ids.contains(&channel_id) {
            println!("channel id {} is already in listening list", channel_id);

            return Ok(());
        }

        if let Some(user) = self
            .seventv_api_client
            .get_user_by_twitch_id(channel_id)
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
            socket
                .send(Message::Text(serde_json::to_string(&data).unwrap()))
                .await?;

            println!("Listening 7TV events for {}'s emote set", user.username);
        }

        Ok(())
    }

    async fn resume_session(
        &mut self,
        socket: &mut WebSocketStream<MaybeTlsStream<TcpStream>>,
    ) -> Result<(), eyre::Error> {
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

        socket
            .send(Message::Text(serde_json::to_string(&data)?))
            .await?;

        Ok(())
    }
}
