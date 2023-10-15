use std::{env, sync::Arc};

use crate::{
    command::CommandLoader, handlers::handle_chat_message, instance_bundle::InstanceBundle,
    localization::Localizator, shared_variables::START_TIME,
};
use eyre::Context;
use reqwest::Client;
use twitch_api::{
    client::ClientDefault,
    twitch_oauth2::{AccessToken, UserToken},
    HelixClient,
};
use twitch_irc::{
    login::StaticLoginCredentials, message::ServerMessage, ClientConfig, SecureTCPTransport,
    TwitchIRCClient,
};

mod command;
mod commands;
mod handlers;
mod instance_bundle;
mod localization;
mod message;
mod models;
mod schema;
mod shared_variables;
mod utils;

#[tokio::main]
async fn main() {
    // Activating static variable
    *START_TIME;

    println!("Hello, world!");
    dotenvy::dotenv().expect("Failed to load .env file");

    let localizator = Arc::new(Localizator::new());
    let command_loader = CommandLoader::new();
    let (mut irc_incoming_messages, irc_client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(
            ClientConfig::new_simple(StaticLoginCredentials::new(
                env::var("BOT_USERNAME")
                    .unwrap_or_else(|_| panic!("No BOT_USERNAME value specified in .env file!")),
                Some(env::var("BOT_OAUTH2_TOKEN").unwrap_or_else(|_| {
                    panic!("No BOT_OAUTH2_TOKEN value specified in .env file!")
                })),
            )),
        );

    let irc_client = Arc::new(irc_client);

    let reqwest_client = Client::default_client_with_name(Some(
        "ilotterytea/bot"
            .parse()
            .wrap_err_with(|| "when creating header name")
            .unwrap(),
    ))
    .wrap_err_with(|| "when creating client")
    .unwrap();

    let helix_token =
        Arc::new(
            match UserToken::from_token(
                &reqwest_client,
                AccessToken::new(env::var("BOT_ACCESS_TOKEN").unwrap_or_else(|_| {
                    panic!("No BOT_ACCESS_TOKEN value specified in .env file!")
                })),
            )
            .await
            {
                Ok(token) => token,
                Err(e) => panic!("Failed to construct user token: {}", e),
            },
        );

    let helix_client = Arc::new(HelixClient::with_client(reqwest_client));

    irc_client.join("imteabot".into()).unwrap();

    let irc_thread = tokio::spawn(async move {
        while let Some(irc_message) = irc_incoming_messages.recv().await {
            match irc_message {
                ServerMessage::Privmsg(message) => {
                    println!("received message: {:?}", message);
                    let instance_bundle = InstanceBundle {
                        twitch_irc_client: irc_client.clone(),
                        twitch_api_client: helix_client.clone(),
                        twitch_api_token: helix_token.clone(),
                        localizator: localizator.clone(),
                    };

                    handle_chat_message(instance_bundle, &command_loader, message).await;
                }
                _ => {
                    println!("not handled message: {:?}", irc_message);
                }
            }
        }
    });

    tokio::join!(irc_thread);
}
