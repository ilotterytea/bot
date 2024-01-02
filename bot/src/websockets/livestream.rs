use std::sync::Arc;

use diesel::PgConnection;
use eyre::Context;
use reqwest::Url;
use tokio::net::TcpStream;
use tokio_tungstenite::{
    connect_async_with_config,
    tungstenite::{protocol::WebSocketConfig, Message},
    MaybeTlsStream, WebSocketStream,
};
use twitch_api::{
    eventsub::{
        stream::{StreamOfflineV1, StreamOnlineV1},
        Event, EventsubWebsocketData, Message as EventsubMessage, Payload, ReconnectPayload,
        SessionData, Transport, WelcomePayload,
    },
    types::UserId,
    TWITCH_EVENTSUB_WEBSOCKET_URL,
};

use crate::{
    handlers::handle_stream_event, instance_bundle::InstanceBundle,
    utils::diesel::establish_connection,
};

use common::models::EventType;

async fn connect(url: Url) -> Result<WebSocketStream<MaybeTlsStream<TcpStream>>, eyre::Error> {
    let config = WebSocketConfig::default();

    let (socket, _) = connect_async_with_config(url, Some(config), false).await?;

    Ok(socket)
}

pub struct TwitchLivestreamClient {
    socket: WebSocketStream<MaybeTlsStream<TcpStream>>,
    session_id: Option<String>,
    instance_bundle: Arc<InstanceBundle>,
    reconnect_url: Url,
}

impl TwitchLivestreamClient {
    pub async fn new(instance_bundle: Arc<InstanceBundle>) -> Result<Self, eyre::Error> {
        let reconnect_url = TWITCH_EVENTSUB_WEBSOCKET_URL.clone();
        Ok(Self {
            instance_bundle,
            socket: connect(reconnect_url.clone()).await?,
            session_id: None,
            reconnect_url,
        })
    }

    pub async fn run(&mut self) -> Result<(), eyre::Error> {
        let conn = &mut establish_connection();

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

                self.process_message(msg, conn).await?
            }
            )
        }
    }

    async fn process_message(
        &mut self,
        msg: Message,
        conn: &mut PgConnection,
    ) -> Result<(), eyre::Error> {
        match msg {
            Message::Text(s) => {
                println!("received text message: {}", s);

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

                    EventsubWebsocketData::Notification { payload, .. } => {
                        let (target_id, event_type) = match payload {
                            Event::StreamOnlineV1(Payload {
                                message: EventsubMessage::Notification(e),
                                ..
                            }) => (e.broadcaster_user_id, EventType::Live),

                            Event::StreamOfflineV1(Payload {
                                message: EventsubMessage::Notification(e),
                                ..
                            }) => (e.broadcaster_user_id, EventType::Offline),

                            _ => {
                                return Ok(());
                            }
                        };

                        handle_stream_event(
                            conn,
                            self.instance_bundle.clone(),
                            target_id,
                            event_type,
                        )
                        .await;
                    }
                    _ => {}
                }
            }
            _ => {}
        }

        Ok(())
    }

    async fn process_welcome_message(
        &mut self,
        session: SessionData<'_>,
    ) -> Result<(), eyre::Report> {
        self.session_id = Some(session.id.to_string());

        if let Some(url) = session.reconnect_url {
            self.reconnect_url = Url::parse(url.to_string().as_str()).unwrap();
        }

        self.process_awaiting_channels().await?;

        Ok(())
    }

    async fn process_awaiting_channels(&mut self) -> Result<(), eyre::Report> {
        let data = self
            .instance_bundle
            .twitch_livestream_websocket_data
            .lock()
            .await;

        if data.awaiting_channel_ids.is_empty() || self.session_id.is_none() {
            return Ok(());
        }

        let channel_ids = data.awaiting_channel_ids.clone();

        let channel_ids = channel_ids
            .iter()
            .filter(|x| !data.listening_channel_ids.contains(&x))
            .collect::<Vec<&UserId>>();

        drop(data);

        for channel_id in channel_ids {
            if let Err(e) = self.listen_channel(channel_id.clone()).await {
                println!(
                    "[TWITCH EVENTSUB] Caught an error on trying to listen a channel ID {}: {}",
                    channel_id.clone().take(),
                    e
                );
            }
        }

        Ok(())
    }

    async fn listen_channel(&mut self, channel_id: UserId) -> Result<(), eyre::Error> {
        let mut data = self
            .instance_bundle
            .twitch_livestream_websocket_data
            .lock()
            .await;

        if let Some(session_id) = self.session_id.clone() {
            let transport = Transport::websocket(session_id);

            self.instance_bundle
                .twitch_api_client
                .create_eventsub_subscription(
                    StreamOnlineV1::broadcaster_user_id(channel_id.clone()),
                    transport.clone(),
                    &*self.instance_bundle.twitch_api_token,
                )
                .await?;

            self.instance_bundle
                .twitch_api_client
                .create_eventsub_subscription(
                    StreamOfflineV1::broadcaster_user_id(channel_id.clone()),
                    transport,
                    &*self.instance_bundle.twitch_api_token,
                )
                .await?;

            if let Some(position) = data
                .awaiting_channel_ids
                .iter()
                .position(|x| x.eq(&channel_id))
            {
                data.awaiting_channel_ids.remove(position);
            }

            data.listening_channel_ids.push(channel_id.clone());

            println!(
                "[TWITCH EVENTSUB] Listening stream events (live/offline) for channel ID {}",
                channel_id.take()
            );

            drop(data);
        }

        Ok(())
    }
}
