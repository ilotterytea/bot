use diesel::{Connection, SqliteConnection};
use managers::command_loader::CommandLoader;
use storage::config::Config;
use tokio::fs;
use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::ServerMessage;
use twitch_irc::TwitchIRCClient;
use twitch_irc::{ClientConfig, SecureTCPTransport};

use crate::arguments::Arguments;

mod arguments;
mod builtin_commands;
mod commands;
mod handlers;
mod managers;
mod schema;

#[tokio::main]
async fn main() {
    let config: Config = toml::from_str(
        fs::read_to_string("./config.toml")
            .await
            .expect("Configuration file does not exist or cannot be read.")
            .as_str(),
    )
    .unwrap();

    let cmdloader = CommandLoader::new();

    let (mut incoming_messages, client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(
            if config.credentials.bot_name.is_none() || config.credentials.oauth_token.is_none() {
                ClientConfig::default()
            } else {
                ClientConfig::new_simple(StaticLoginCredentials::new(
                    config.credentials.bot_name.unwrap(),
                    Some(config.credentials.oauth_token.unwrap()),
                ))
            },
        );

    client.join("ilotterytea".to_owned()).unwrap();

    let join_handle = tokio::spawn(async move {
        while let Some(message) = incoming_messages.recv().await {
            println!("Received message: {:?}", message);
            match message {
                ServerMessage::Privmsg(msg) => {
                    handlers::irc_message_handler(Arguments {
                        message: msg,
                        client: &client,
                        loader: &cmdloader,
                    })
                    .await;
                }
                _ => {}
            }
        }
    });

    join_handle.await.unwrap();
}

pub fn establish_connection() -> SqliteConnection {
    dotenvy::dotenv().ok();

    let database_url = std::env::var("DATABASE_URL").expect("DATABASE_URL must be set!");
    SqliteConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Error connecting to {}", database_url))
}
