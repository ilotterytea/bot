use std::sync::Arc;

use eyre::Context;
use futures_util::SinkExt;
use tokio::net::TcpStream;
use tokio_tungstenite::{
    connect_async_with_config, tungstenite,
    tungstenite::{protocol::WebSocketConfig, Message},
    MaybeTlsStream, WebSocketStream,
};
use twitch_api::{twitch_oauth2::UserToken, types::UserId, HelixClient};
use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

use crate::seventv::schemes::{Dispatch, Hello, Payload};

use super::schemes::Resume;

pub struct SevenTVWebsocketClient {
    pub client: Option<Arc<WebSocketStream<MaybeTlsStream<TcpStream>>>>,
    pub irc_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    pub helix_token: Arc<UserToken>,
    pub helix_client: Arc<HelixClient<'static, reqwest::Client>>,
    pub awaiting_channel_ids: Vec<UserId>,
    pub listening_channel_ids: Vec<UserId>,
    pub session_id: Option<String>,
    pub connect_url: url::Url,
}

impl SevenTVWebsocketClient {
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

                if let Ok(e) = serde_json::from_str::<Payload<String>>(s.as_str()) {
                    match e.op {
                        // Dispatch
                        0 => {
                            if let Ok(d) = serde_json::from_str::<Dispatch>(e.d.as_str()) {
                                if d.event_type != "emote_set.update".to_string() {
                                    return Ok(());
                                }
                            }
                        }
                        // Hello
                        1 => {
                            if let Ok(d) = serde_json::from_str::<Hello>(e.d.as_str()) {
                                if self.session_id.is_none() {
                                    self.session_id = Some(d.session_id);
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

    pub async fn process_awaiting_channels(&mut self) -> Result<(), eyre::Error> {
        if self.awaiting_channel_ids.is_empty() {
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

        let position = self
            .awaiting_channel_ids
            .iter()
            .position(|x| x.eq(&channel_id))
            .unwrap();

        self.awaiting_channel_ids.remove(position);
        self.listening_channel_ids.push(channel_id.clone());

        println!("Listening 7TV events for channel ID {}", channel_id.take());

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
