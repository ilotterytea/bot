use std::{env, sync::Arc, time::Duration};

use crate::{
    commands::CommandLoader,
    config::{Configuration, BOT_CONFIGURATION_FILE},
    handlers::{handle_chat_message, handle_timers},
    instance_bundle::InstanceBundle,
    localization::Localizator,
    seventv::{api::SevenTVAPIClient, SevenTVWebsocketClient},
    shared_variables::{START_TIME, TIMER_CHECK_DELAY},
};

use common::{
    establish_connection,
    models::NewChannel,
    schema::{channels::dsl as ch, events::dsl as ev},
};
use diesel::{insert_into, update, ExpressionMethods, QueryDsl, RunQueryDsl};
use eyre::Context;
use log::{debug, error, info};
use reqwest::Client;
use tokio::sync::Mutex;
use twitch_api::{
    client::ClientDefault,
    twitch_oauth2::{AccessToken, UserToken},
    types::{UserId, UserIdRef},
    HelixClient,
};
use twitch_irc::{
    login::StaticLoginCredentials, message::ServerMessage, ClientConfig, SecureTCPTransport,
    TwitchIRCClient,
};
use websockets::{livestream::TwitchLivestreamClient, WebsocketData};

mod commands;
mod config;
mod handlers;
mod instance_bundle;
mod localization;
mod message;
mod models;
mod modules;
mod seventv;
mod shared_variables;
mod utils;
mod websockets;

#[tokio::main]
async fn main() {
    // Activating static variable
    *START_TIME;

    env_logger::init();

    info!("Starting Twitch bot...");

    let config = match toml::from_str::<Configuration>(BOT_CONFIGURATION_FILE) {
        Ok(v) => v,
        Err(e) => panic!("Failed to parse TOML configuration file: {}", e),
    };

    dotenvy::dotenv().expect("Failed to load .env file");

    let localizator = Arc::new(Localizator::new());
    let command_loader = CommandLoader::new();
    let (mut irc_incoming_messages, irc_client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(
            ClientConfig::new_simple(StaticLoginCredentials::new(
                config.credentials.username.clone(),
                Some(config.credentials.password.clone()),
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

    let helix_token = Arc::new(
        match UserToken::from_token(
            &reqwest_client,
            AccessToken::from(config.credentials.password.clone()),
        )
        .await
        {
            Ok(token) => token,
            Err(e) => panic!("Failed to construct user token: {}", e),
        },
    );

    let helix_client = Arc::new(HelixClient::with_client(reqwest_client));

    let conn = &mut establish_connection();

    let mut channels: Vec<common::models::Channel> = ch::channels
        .filter(ch::opt_outed_at.is_null())
        .load::<common::models::Channel>(conn)
        .expect("Failed to get alias IDs");

    let bot_user_id = helix_token.user_id.clone().take().parse::<i32>().unwrap();

    if !channels.iter().any(|x| x.alias_id == bot_user_id) {
        channels.push(
            insert_into(ch::channels)
                .values([NewChannel {
                    alias_id: bot_user_id,
                    alias_name: helix_token.login.clone().take(),
                }])
                .get_result(conn)
                .expect("Failed to create a bot channel data"),
        );
    }

    for mut channel in channels {
        if let Ok(Some(user)) = helix_client
            .get_user_from_id(
                UserIdRef::from_str(channel.alias_id.to_string().as_str()),
                &*helix_token,
            )
            .await
        {
            let login = user.login.to_string();

            if channel.alias_name.ne(&login) {
                update(ch::channels.find(&channel.id))
                    .set(ch::alias_name.eq(&login))
                    .execute(conn)
                    .expect("Failed to update channel name");

                channel.alias_name = login.clone();
            }

            irc_client.join(login).expect("Failed to join a channel");

            info!(
                "Joined chat room: ID: {}, alias ID: {}, alias name: {}",
                channel.id, channel.alias_id, channel.alias_name
            );
        } else {
            error!(
                "Failed to join chat room: ID: {}, alias ID: {}, alias name: {}",
                channel.id, channel.alias_id, channel.alias_name
            );
        }
    }
    let livestream_data = Arc::new(Mutex::new({
        let mut data = WebsocketData::default();

        let ids = ev::events
            .filter(ev::target_alias_id.is_not_null())
            .select(ev::target_alias_id)
            .load::<Option<i32>>(conn)
            .expect("Failed to get events");

        let ids = ids
            .iter()
            .map(|x| UserId::new(x.unwrap().to_string()))
            .collect::<Vec<UserId>>();

        data.awaiting_channel_ids.extend(ids);

        data
    }));

    let seventv_data = Arc::new(Mutex::new({
        let mut data = WebsocketData::default();

        let ids = ch::channels
            .filter(ch::opt_outed_at.is_null())
            .select(ch::alias_id)
            .load::<i32>(conn)
            .expect("Failed to get channels");

        let ids = ids
            .iter()
            .map(|x| UserId::new(x.to_string()))
            .collect::<Vec<UserId>>();

        data.awaiting_channel_ids.extend(ids);

        data
    }));

    let seventv_api = Arc::new(SevenTVAPIClient::new(Client::new()));

    let instances = Arc::new(InstanceBundle {
        twitch_irc_client: irc_client.clone(),
        twitch_api_token: helix_token.clone(),
        twitch_api_client: helix_client.clone(),
        localizator: localizator.clone(),
        twitch_livestream_websocket_data: livestream_data.clone(),
        seventv_api_client: seventv_api.clone(),
        seventv_eventapi_data: seventv_data.clone(),
    });

    let timer_thread = tokio::spawn({
        let instances = instances.clone();
        async move {
            loop {
                handle_timers(&instances).await;
                tokio::time::sleep(Duration::from_secs(TIMER_CHECK_DELAY)).await;
            }
        }
    });

    let mut livestream_client = TwitchLivestreamClient::new(instances.clone())
        .await
        .unwrap();

    let livestream_thread = tokio::spawn(async move {
        livestream_client.run().await.unwrap();
    });

    let mut seventv_client = SevenTVWebsocketClient::new(instances.clone())
        .await
        .unwrap();

    let seventv_thread = tokio::spawn(async move {
        seventv_client.run().await.unwrap();
    });

    let irc_thread = tokio::spawn(async move {
        while let Some(irc_message) = irc_incoming_messages.recv().await {
            match irc_message {
                ServerMessage::Privmsg(message) => {
                    println!("received message: {:?}", message);
                    let instances = instances.clone();

                    handle_chat_message(instances, &command_loader, message).await;
                }
                _ => {
                    println!("not handled message: {:?}", irc_message);
                }
            }
        }
    });

    tokio::join!(irc_thread, timer_thread, livestream_thread, seventv_thread);
}
