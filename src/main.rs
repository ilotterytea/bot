use std::sync::Arc;

use crate::{command::CommandLoader, handlers::handle_chat_message, localization::Localizator};
use twitch_irc::{
    login::StaticLoginCredentials, message::ServerMessage, ClientConfig, SecureTCPTransport,
    TwitchIRCClient,
};

mod command;
mod handlers;
mod localization;

#[tokio::main]
async fn main() {
    println!("Hello, world!");

    let localizator = Arc::new(Localizator::new());
    let command_loader = CommandLoader::new();
    let (mut irc_incoming_messages, irc_client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(ClientConfig::default());

    let irc_client = Arc::new(irc_client);

    irc_client.join("ilotterytea".into()).unwrap();

    let irc_thread = tokio::spawn(async move {
        while let Some(irc_message) = irc_incoming_messages.recv().await {
            match irc_message {
                ServerMessage::Privmsg(message) => {
                    println!("received message: {:?}", message);
                    handle_chat_message(&command_loader).await;
                }
                _ => {
                    println!("not handled message: {:?}", irc_message);
                }
            }
        }
    });

    tokio::join!(irc_thread);
}
