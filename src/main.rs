use crate::api::command::CommandLoader;
use crate::api::InstanceBundle;
use crate::handlers::irc_message_handler;
use crate::livestream::EventsubLivestreamClient;
use crate::schema::{channels::dsl as ch, events::dsl as ev};
use crate::shared_variables::START_TIME;
use crate::utils::establish_connection;
use crate::utils::twitch::get_access_token;
use diesel::prelude::*;
use eyre::Context;
use reqwest::Client;
use seventv::websocket::SevenTVWebsocketClient;
use std::env;
use std::sync::Arc;
use tokio::sync::Mutex;
use twitch_api::client::ClientDefault;
use twitch_api::types::{UserId, UserIdRef};
use twitch_api::{HelixClient, TWITCH_EVENTSUB_WEBSOCKET_URL};
use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::ServerMessage;
use twitch_irc::TwitchIRCClient;
use twitch_irc::{ClientConfig, SecureTCPTransport};

mod api;
mod commands;
mod handlers;
mod livestream;
mod locale;
mod models;
mod schema;
mod seventv;
mod shared_variables;
mod utils;

#[tokio::main]
pub async fn main() -> Result<(), eyre::Report> {
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

    let client = Arc::new(client);

    let command_loader = Arc::new(Mutex::from(CommandLoader::new()));

    let helix_client = Arc::new(HelixClient::with_client(
        Client::default_client_with_name(Some(
            "ilotterytea/bot"
                .parse()
                .wrap_err_with(|| "when creating header name")
                .unwrap(),
        ))
        .wrap_err_with(|| "when creating client")?,
    ));

    let helix_token = Arc::new(
        get_access_token(
            helix_client.get_client(),
            Some(
                env::var("BOT_ACCESS_TOKEN")
                    .expect("BOT_ACCESS_TOKEN must be set for Twitch API requests"),
            ),
            None,
            None,
            None,
        )
        .await?,
    );

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

    let conn = &mut establish_connection();

    // Select channel IDs from the DB and join those channels
    let channel_ids = ch::channels
        .select(ch::alias_id)
        .load::<i32>(conn)
        .expect("Failed to load channel IDs");

    for channel_id in channel_ids {
        let _channel_id = channel_id.to_string();
        let _channel_ids: &[&UserIdRef] = &[_channel_id.as_str().into()];

        let user = &helix_client
            .get_user_from_id(UserIdRef::from_str(_channel_id.as_str()), &*helix_token)
            .await
            .unwrap();

        if user.is_some() {
            let u = user.clone().unwrap();

            if client.join(u.login.to_string()).is_ok() {
                println!("Successfully joined #{}", u.login);
            }
        }
    }

    // Getting all the stream events
    let event_channel_ids = ev::events
        .filter(ev::target_alias_id.is_not_null())
        .select(ev::target_alias_id)
        .load::<Option<i32>>(conn)
        .expect("Failed to load the event target alias IDs");

    let initial_eventsub_channel_ids: Vec<UserId> = event_channel_ids
        .iter()
        .map(|x| UserId::new(x.unwrap().to_string()))
        .collect::<Vec<UserId>>();

    let eventsub_client = EventsubLivestreamClient {
        session_id: None,
        irc_client: client.clone(),
        token: helix_token.clone(),
        client: helix_client.clone(),
        connect_url: TWITCH_EVENTSUB_WEBSOCKET_URL.clone(),
        listening_channel_ids: Vec::new(),
        awaiting_channel_ids: initial_eventsub_channel_ids,
    };

    let eventsub_handle = tokio::spawn(async move { eventsub_client.run().await });

    let seventv = SevenTVWebsocketClient {
        session_id: None,
        helix_client: helix_client.clone(),
        helix_token: helix_token.clone(),
        irc_client: client.clone(),
        awaiting_channel_ids: Vec::new(),
        listening_channel_ids: Vec::new(),
        connect_url: url::Url::parse("wss://events.7tv.io/v3").unwrap(),
    };

    let seventv_handle = tokio::spawn(async move { seventv.run().await });

    // The handler for Twitch chat client
    let join_handle = tokio::spawn(async move {
        while let Some(message) = incoming_messages.recv().await {
            let instance_bundle = InstanceBundle {
                twitch_client: client.clone(),
                twitch_api_client: helix_client.clone(),
                twitch_api_token: helix_token.clone(),
            };
            if let ServerMessage::Privmsg(msg) = message {
                irc_message_handler(instance_bundle, command_loader.lock().await, msg).await;
            }
        }
    });

    let _ = eventsub_handle.await.unwrap();

    let _ = seventv_handle.await.unwrap();

    // keep the tokio executor alive.
    // If you return instead of waiting the background task will exit.
    join_handle.await.unwrap();

    Ok(())
}
