use std::sync::Arc;

use eyre::Context;
use itertools::Itertools;
use tokio::net::TcpStream;
use tokio_tungstenite::{
    connect_async_with_config, tungstenite,
    tungstenite::{protocol::WebSocketConfig, Message},
    MaybeTlsStream, WebSocketStream,
};
use twitch_api::{
    eventsub::{
        event::websocket::{EventsubWebsocketData, ReconnectPayload, SessionData, WelcomePayload},
        stream::{StreamOfflineV1, StreamOnlineV1},
        Event, Message as EventsubMessage, Payload, Transport,
    },
    twitch_oauth2::UserToken,
    types::UserId,
    HelixClient,
};
use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

use crate::{handlers::handle_stream_event, models::diesel::EventType};

pub struct EventsubLivestreamClient {
    pub session_id: Option<String>,
    pub irc_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    pub token: Arc<UserToken>,
    pub client: Arc<HelixClient<'static, reqwest::Client>>,
    pub awaiting_channel_ids: Vec<UserId>,
    pub listening_channel_ids: Vec<UserId>,
    pub connect_url: url::Url,
}

impl EventsubLivestreamClient {
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

    pub async fn run(&mut self) -> Result<(), eyre::Error> {
        let mut s = self
            .connect()
            .await
            .context("when establishing connection")?;

        loop {
            self.awaiting_channel_ids = self
                .awaiting_channel_ids
                .iter()
                .cloned()
                .unique()
                .collect::<Vec<UserId>>();

            self.process_awaiting_channels().await?;

            tokio::select!(
                Some(msg) = futures::StreamExt::next(&mut s) => {
                    let msg = match msg {
                        Err(tungstenite::Error::Protocol(tungstenite::error::ProtocolError::ResetWithoutClosingHandshake)) => {
                            s = self.connect().await.context("when reestablishing connection")?;
                            continue
                        }
                        _ => msg.context("when getting message")?,
                    };
                    self.process_message(msg).await?
                }
            )
        }
    }

    pub async fn process_message(&mut self, msg: Message) -> Result<(), eyre::Report> {
        match msg {
            Message::Text(s) => {
                println!("received text message: {s}");

                match Event::parse_websocket(&s)? {
                    EventsubWebsocketData::Welcome {
                        payload: WelcomePayload { session },
                        ..
                    }
                    | EventsubWebsocketData::Reconnect {
                        payload: ReconnectPayload { session },
                        ..
                    } => {
                        self.process_welcome_message(session).await?;
                    }
                    EventsubWebsocketData::Notification {
                        metadata: _,
                        payload,
                    } => match payload {
                        Event::StreamOnlineV1(Payload { message, .. }) => {
                            println!("got live event: {message:?}");

                            match message {
                                EventsubMessage::Notification(e) => {
                                    handle_stream_event(
                                        self.irc_client.clone(),
                                        self.client.clone(),
                                        self.token.clone(),
                                        e.broadcaster_user_id,
                                        EventType::Live,
                                    )
                                    .await
                                }
                                _ => {}
                            }
                        }
                        Event::StreamOfflineV1(Payload { message, .. }) => {
                            println!("got offline event: {message:?}");

                            match message {
                                EventsubMessage::Notification(e) => {
                                    handle_stream_event(
                                        self.irc_client.clone(),
                                        self.client.clone(),
                                        self.token.clone(),
                                        e.broadcaster_user_id,
                                        EventType::Offline,
                                    )
                                    .await
                                }
                                _ => {}
                            }
                        }
                        _ => {}
                    },
                    EventsubWebsocketData::Revocation {
                        metadata,
                        payload: _,
                    } => eyre::bail!("got revocation event: {metadata:?}"),
                    EventsubWebsocketData::Keepalive {
                        metadata: _,
                        payload: _,
                    } => {}
                    _ => {}
                }
            }
            Message::Close(e) => {
                let e = if e.is_some() {
                    let unwrapped_e = e.unwrap();

                    format!("{} {}", unwrapped_e.code, unwrapped_e.reason)
                } else {
                    "No reason".to_string()
                };

                println!("The connection to Twitch EventSub was refused: {e}");
            }
            _ => {}
        }

        Ok(())
    }

    pub async fn process_welcome_message(
        &mut self,
        data: SessionData<'_>,
    ) -> Result<(), eyre::Report> {
        self.session_id = Some(data.id.to_string());

        if let Some(url) = data.reconnect_url {
            self.connect_url = url.parse()?;
        }

        self.process_awaiting_channels().await?;

        Ok(())
    }

    pub async fn process_awaiting_channels(&mut self) -> Result<(), eyre::Error> {
        if self.awaiting_channel_ids.is_empty() || self.session_id.is_none() {
            return Ok(());
        }

        let awaiting_channel_ids = self.awaiting_channel_ids.clone();

        for channel_id in awaiting_channel_ids {
            self.listen_channel(channel_id).await?;
        }

        Ok(())
    }

    pub async fn listen_channel(&mut self, channel_id: UserId) -> Result<(), eyre::Error> {
        if self.listening_channel_ids.contains(&channel_id) {
            println!(
                "channel id {} is already in listening list",
                channel_id.take()
            );

            return Ok(());
        }

        if self.session_id.is_none() {
            if !self.awaiting_channel_ids.contains(&channel_id) {
                self.awaiting_channel_ids.push(channel_id.clone());
                println!(
                    "channel id {} was pushed to awaiting list because session id is none",
                    channel_id
                );
            }

            return Ok(());
        }

        let transport = Transport::websocket(self.session_id.clone().unwrap());

        self.client
            .create_eventsub_subscription(
                StreamOnlineV1::broadcaster_user_id(channel_id.clone()),
                transport.clone(),
                &*self.token,
            )
            .await?;

        self.client
            .create_eventsub_subscription(
                StreamOfflineV1::broadcaster_user_id(channel_id.clone()),
                transport,
                &*self.token,
            )
            .await?;

        let position = self
            .awaiting_channel_ids
            .iter()
            .position(|x| x.eq(&channel_id))
            .unwrap();

        self.awaiting_channel_ids.remove(position);
        self.listening_channel_ids.push(channel_id.clone());

        println!(
            "Listening stream events (live/offline) for channel ID {}",
            channel_id.take()
        );

        Ok(())
    }
}
