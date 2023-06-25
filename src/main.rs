use crate::api::command::CommandLoader;
use crate::handlers::irc_message_handler;
use crate::shared_variables::START_TIME;
use std::env;
use std::sync::Arc;
use tokio::sync::Mutex;
use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::ServerMessage;
use twitch_irc::TwitchIRCClient;
use twitch_irc::{ClientConfig, SecureTCPTransport};

mod api;
mod commands;
mod handlers;
mod locale;
mod models;
mod schema;
mod shared_variables;
mod utils;

#[tokio::main]
pub async fn main() {
    println!("{:?}", *START_TIME);
    dotenvy::dotenv().ok();

    // default configuration is to join chat as anonymous.
    let (mut incoming_messages, client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(
            ClientConfig::new_simple(StaticLoginCredentials::new(
                env::var("BOT_NAME")
                    .unwrap_or_else(|_| panic!("No BOT_NAME value specified in .env file!")),
                Some(env::var("BOT_OAUTH2_TOKEN").unwrap_or_else(|_| {
                    panic!("No BOT_OAUTH2_TOKEN value specified in .env file!")
                })),
            )),
        );

    let command_loader = Arc::new(Mutex::from(CommandLoader::new()));

    // join a channel
    // This function only returns an error if the passed channel login name is malformed,
    // so in this simple case where the channel name is hardcoded we can ignore the potential
    // error with `unwrap`.
    client
        .join(
            env::var("BOT_NAME")
                .unwrap_or_else(|_| panic!("No BOT_NAME value specified in .env file!")),
        )
        .unwrap();

    let join_handle = tokio::spawn(async move {
        while let Some(message) = incoming_messages.recv().await {
            if let ServerMessage::Privmsg(msg) = message {
                irc_message_handler(&client, command_loader.lock().await, msg).await;
            }
        }
    });

    // keep the tokio executor alive.
    // If you return instead of waiting the background task will exit.
    join_handle.await.unwrap();
}
