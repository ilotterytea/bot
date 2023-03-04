use diesel::{Connection, SqliteConnection};
use managers::command_loader::CommandLoader;
use std::env;
use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::ServerMessage;
use twitch_irc::TwitchIRCClient;
use twitch_irc::{ClientConfig, SecureTCPTransport};

mod arguments;
mod builtin_commands;
mod commands;
mod handlers;
mod managers;
mod models;
mod schema;

#[tokio::main]
async fn main() {
    dotenvy::dotenv().ok();

    let cmdloader = CommandLoader::new();

    let (mut incoming_messages, client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(
            if env::var("BOT_NAME").is_err() || env::var("OAUTH2_TOKEN").is_err() {
                ClientConfig::default()
            } else {
                ClientConfig::new_simple(StaticLoginCredentials::new(
                    env::var("BOT_NAME").unwrap(),
                    Some(env::var("OAUTH2_TOKEN").unwrap()),
                ))
            },
        );

    client.join("ilotterytea".to_owned()).unwrap();

    let join_handle = tokio::spawn(async move {
        while let Some(message) = incoming_messages.recv().await {
            println!("Received message: {:?}", message);
            match message {
                ServerMessage::Privmsg(msg) => {
                    handlers::irc_message_handler(&client, &cmdloader, msg).await;
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
