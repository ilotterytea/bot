use std::{collections::HashSet, process::exit, sync::Arc, time::Duration};

use crate::{
    commands::CommandLoader, handlers::*, instance_bundle::InstanceBundle,
    localization::Localizator, shared_variables::TIMER_CHECK_DELAY,
};

use common::{
    config::Configuration,
    establish_connection,
    models::{ChannelFeature, ChannelPreference, NewChannel},
    schema::{channels::dsl as ch, events::dsl as ev},
};
use diesel::{
    BelongingToDsl, Connection, ExpressionMethods, PgConnection, QueryDsl, RunQueryDsl,
    insert_into, update,
};
use livestream::TwitchLivestreamHelper;
use log::{debug, error, info};
use reqwest::{Client, multipart::Form};
use tokio::sync::Mutex;
use twitch_api::{
    HelixClient,
    client::ClientDefault,
    twitch_oauth2::{AccessToken, UserToken},
    types::{UserId, UserIdRef},
};
use twitch_emotes::{
    betterttv::BetterTTVWSClient,
    emotes::RetrieveEmoteWS,
    seventv::{SevenTVAPIClient, SevenTVWSClient},
};
use twitch_irc::{
    ClientConfig, SecureTCPTransport, TwitchIRCClient, login::StaticLoginCredentials,
    message::ServerMessage,
};

mod commands;
mod handlers;
mod instance_bundle;
mod livestream;
mod localization;
mod models;
mod modules;
//mod seventv;
mod shared_variables;
mod utils;

#[tokio::main]
async fn main() {
    let config = Configuration::load();

    env_logger::init();

    unsafe {
        std::env::set_var("DATABASE_URL", config.database.url.clone());
        std::env::set_var(
            "BOT_START_TIMESTAMP",
            chrono::Utc::now()
                .naive_utc()
                .and_utc()
                .timestamp()
                .to_string(),
        );
    }

    info!("Starting Twitch bot...");

    match PgConnection::establish(&config.database.url) {
        Ok(v) => {
            info!("PostgreSQL connection looks good!");
            drop(v);
        }
        Err(_) => {
            error!(
                "Failed to connect to PostgreSQL database on {}",
                config.database.url
            );
            exit(1);
        }
    }

    let localizator = Arc::new(Localizator::new());

    let (mut irc_incoming_messages, irc_client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(
            ClientConfig::new_simple(StaticLoginCredentials::new(
                config.bot.username.clone(),
                Some(config.bot.password.clone()),
            )),
        );

    let irc_client = Arc::new(irc_client);

    let reqwest_client =
        Client::default_client_with_name(Some("ilotterytea/bot".parse().unwrap())).unwrap();

    let helix_token = Arc::new(
        match UserToken::from_token(
            &reqwest_client,
            AccessToken::from(config.bot.password.clone()),
        )
        .await
        {
            Ok(token) => token,
            Err(e) => panic!("Failed to construct user token: {}", e),
        },
    );

    let helix_client = Arc::new(HelixClient::with_client(reqwest_client));

    let stv_api_client = SevenTVAPIClient::new();
    let (mut stv_messages, mut stv_client) = SevenTVWSClient::new()
        .await
        .expect("Error creating a 7TV Instance");

    let (mut bttv_messages, mut bttv_client) = BetterTTVWSClient::new()
        .await
        .expect("Error creating a BTTV instance");

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

            // Adding channel to stats
            if let Some(stats_password) = &config.third_party.stats_api_password {
                let client = Client::new();
                let _ = client
                    .post(format!(
                        "{}/api/users/join",
                        &config.third_party.stats_api_url
                    ))
                    .header("Authorization", format!("Statea {}", stats_password))
                    .multipart(Form::new().text("username", channel.alias_name.clone()))
                    .send()
                    .await;
            }

            // Subscribing the channel to 7TV events
            if let Ok(features) = ChannelPreference::belonging_to(&channel)
                .select(common::schema::channel_preferences::dsl::features)
                .get_result::<Vec<Option<String>>>(conn)
            {
                let features: Vec<String> = features.into_iter().flatten().collect();

                if features.contains(&ChannelFeature::Notify7TVUpdates.to_string()) {
                    if let Some(stv_user) = stv_api_client
                        .get_user_by_twitch_id(channel.alias_id as usize)
                        .await
                    {
                        stv_client.subscribe_emote_set(stv_user.emote_set_id.clone());
                    }
                }

                if features.contains(&ChannelFeature::NotifyBTTVUpdates.to_string()) {
                    bttv_client.join_channel(channel.alias_id as usize);
                }
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

    let stv_api_client = Arc::new(stv_api_client);
    let stv_client = Arc::new(Mutex::new(stv_client));

    let stv_thread = tokio::spawn({
        let stv_client = stv_client.clone();
        async move {
            loop {
                let mut stv_client = stv_client.lock().await;
                stv_client
                    .process(&mut stv_messages)
                    .await
                    .expect("Error processing 7TV messages");
            }
        }
    });

    let bttv_client = Arc::new(Mutex::new(bttv_client));

    let bttv_thread = tokio::spawn({
        let bttv_client = bttv_client.clone();
        async move {
            loop {
                let mut bttv_client = bttv_client.lock().await;
                bttv_client
                    .process(&mut bttv_messages)
                    .await
                    .expect("Error processing BTTV messages");
            }
        }
    });

    let livestream_data = Arc::new(Mutex::new({
        let ids = ev::events
            .filter(ev::target_alias_id.is_not_null())
            .select(ev::target_alias_id)
            .load::<Option<i32>>(conn)
            .expect("Failed to get events");

        ids.iter()
            .map(|x| UserId::new(x.unwrap().to_string()))
            .collect::<HashSet<UserId>>()
    }));

    let config = Arc::new(config);

    let instances = Arc::new(InstanceBundle {
        twitch_irc_client: irc_client.clone(),
        twitch_api_token: helix_token.clone(),
        twitch_api_client: helix_client.clone(),
        localizator: localizator.clone(),
        configuration: config.clone(),
        twitch_livestream_websocket_data: livestream_data.clone(),
        stv_client: stv_client.clone(),
        stv_api_client: stv_api_client.clone(),
        bttv_client: bttv_client.clone(),
    });

    // Setting up 7TV WS client handlers
    let mut stv_client = stv_client.lock().await;
    stv_client.on_emote_create(handlers::emotes::handle_seventv_emote_event(
        instances.clone(),
        localization::LineId::EmotesPushed,
    ));
    stv_client.on_emote_delete(handlers::emotes::handle_seventv_emote_event(
        instances.clone(),
        localization::LineId::EmotesPulled,
    ));
    stv_client.on_emote_update(handlers::emotes::handle_seventv_emote_event(
        instances.clone(),
        localization::LineId::EmotesUpdated,
    ));
    drop(stv_client);

    // Setting up BTTV WS client handlers
    let mut bttv_client = bttv_client.lock().await;
    bttv_client.on_emote_create(handlers::emotes::handle_betterttv_emote_event(
        instances.clone(),
        localization::LineId::EmotesPushed,
    ));
    bttv_client.on_emote_delete(handlers::emotes::handle_betterttv_emote_event(
        instances.clone(),
        localization::LineId::EmotesPulled,
    ));
    bttv_client.on_emote_update(handlers::emotes::handle_betterttv_emote_event(
        instances.clone(),
        localization::LineId::EmotesUpdated,
    ));
    drop(bttv_client);

    let mut command_loader = CommandLoader::new(instances.clone());
    command_loader.load().await.expect("Error loading commands");
    let command_loader = Arc::new(Mutex::new(command_loader));

    #[cfg(debug_assertions)]
    CommandLoader::enable_hot_reloading(command_loader.clone());

    let timer_thread = tokio::spawn({
        let instances = instances.clone();
        async move {
            loop {
                handle_timers(&instances).await;
                tokio::time::sleep(Duration::from_secs(TIMER_CHECK_DELAY)).await;
            }
        }
    });

    let mut livestream_helper = TwitchLivestreamHelper::new(instances.clone());

    let livestream_thread = tokio::spawn(async move {
        livestream_helper.run().await;
    });

    let irc_thread = tokio::spawn(async move {
        while let Some(irc_message) = irc_incoming_messages.recv().await {
            debug!("Received IRC message: {:?}", irc_message);

            let instances = instances.clone();
            let command_loader = command_loader.clone();

            tokio::spawn(async move {
                match irc_message {
                    ServerMessage::Privmsg(message) => {
                        handle_chat_message(instances, command_loader, message).await;
                    }
                    ServerMessage::Notice(message) => {
                        handle_notice_message(instances, message).await;
                    }
                    _ => {}
                }
            });
        }
    });

    let _ = tokio::join!(
        irc_thread,
        timer_thread,
        livestream_thread,
        stv_thread,
        bttv_thread
    );
}
