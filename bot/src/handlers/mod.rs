use std::{collections::HashSet, sync::Arc};

use chrono::Utc;
use diesel::{insert_into, update, BelongingToDsl, BoolExpressionMethods, ExpressionMethods, PgConnection, QueryDsl, RunQueryDsl};
use log::info;
use reqwest::{multipart::Form, Client};
use substring::Substring;
use tokio::sync::Mutex;
use twitch_api::{helix::chat::GetChattersRequest, types::UserId};
use twitch_irc::message::{NoticeMessage, PrivmsgMessage};

use crate::{
    commands::{request::Request, response::Response, CommandLoader},
    instance_bundle::InstanceBundle,
    utils::split_and_wrap_lines,
};

use common::{
    establish_connection, models::{
        Channel, ChannelFeature, CustomCommand, Event, EventFlag, EventSubscription, EventType, NewAction, Timer, User
    }, schema::{
        actions::dsl as ac, channel_preferences::dsl as chp, channels::dsl as ch, custom_commands::dsl as cc, events::dsl as ev, timers::dsl as ti, users::dsl as us
    }
};

pub mod emotes;

pub async fn handle_chat_message(
    instance_bundle: Arc<InstanceBundle>,
    command_loader: Arc<Mutex<CommandLoader>>,
    message: PrivmsgMessage,
) {
    let conn = &mut establish_connection();
    handle_stalk_message_events(conn, instance_bundle.clone(), message.clone()).await;

    let command_loader = command_loader.lock().await;

    if let Some(request) = Request::try_from(&instance_bundle.configuration.commands, &message, &command_loader, conn) {
        execute_command(&command_loader, &instance_bundle, &message, request.clone()).await;
    }

    handle_custom_commands(conn, &instance_bundle, &message, &command_loader).await;
}

pub async fn handle_timers(instance_bundle: &InstanceBundle) {
    let current_timestamp = Utc::now().naive_utc();
    let conn = &mut establish_connection();
    let channels = ch::channels
        .filter(ch::opt_outed_at.is_null())
        .load::<Channel>(conn)
        .expect("Failed to get channels");

    for channel in channels {
        if let Ok(features) = chp::channel_preferences
            .filter(chp::channel_id.eq(&channel.id))
            .select(chp::features)
            .get_result::<Vec<Option<String>>>(conn)
        {
            if features.iter().flatten().any(|x| x.eq(&ChannelFeature::SilentMode.to_string())) {
                continue;
            }
        }

        let timers = Timer::belonging_to(&channel)
            .filter(ti::is_enabled.eq(true))
            .load::<Timer>(conn)
            .expect("Failed to get timers for channel ID ");

        for timer in timers {
            if current_timestamp.and_utc().timestamp() - timer.last_executed_at.and_utc().timestamp()
                < timer.interval_sec
            {
                continue;
            }

            for line in timer.messages {
                let mut split = line.split(' ').collect::<Vec<&str>>();
                let first_line = split[0];
                split.remove(0);

                let s_line = split.join(" ");

                if s_line.is_empty() {
                    continue;
                }

                let channel_name = channel.alias_name.clone();

                match first_line {
                    "/me" => instance_bundle.twitch_irc_client.me(channel_name, s_line).await.expect("Failed to send a message"),
                    _ => instance_bundle.twitch_irc_client.say(channel_name, line).await.expect("Failed to send a message")
                };
            }

            update(ti::timers.filter(ti::id.eq(timer.id)))
                .set(ti::last_executed_at.eq(current_timestamp))
                .execute(conn)
                .expect("Failed to update the timer with ID");
        }
    }
}

pub async fn handle_custom_commands(
    conn: &mut PgConnection,
    instance_bundle: &InstanceBundle,
    message: &PrivmsgMessage,
    command_loader: &CommandLoader
) {
    let message_text = message.message_text.clone();
    let parts = message_text.split(' ').collect::<Vec<&str>>();

    if parts.is_empty() {
        return;
    }

    let command_id = parts[0];

    let alias_id = message.channel_id.parse::<i32>().unwrap();
    let Ok(channel) = ch::channels
        .filter(ch::alias_id.eq(&alias_id))
        .get_result::<Channel>(conn)
    else {
        return;
    };

    if let Ok(features) = chp::channel_preferences
        .filter(chp::channel_id.eq(&channel.id))
        .select(chp::features)
        .get_result::<Vec<Option<String>>>(conn)
    {
        if features.iter().flatten().any(|x| x.eq(&ChannelFeature::SilentMode.to_string())) {
            return;
        }
    }

    let Ok(command) = cc::custom_commands
        .filter(cc::name.eq(command_id))
        .filter(cc::is_enabled.eq(true))
        .filter(cc::channel_id.eq(&channel.id).or(cc::is_global.eq(true)))
        .get_result::<CustomCommand>(conn)
    else {
        return;
    };
        
    for line in &command.messages {
        let mut line = line.clone();

        if line.starts_with("\\!") {
            line = line.substring(1, line.len()).to_string();
        } else if line.starts_with("!") {
            let mut message = message.clone();
            message.message_text = line.clone();

            let Some(request) = Request::try_from(&instance_bundle.configuration.commands, &message, command_loader, conn) else {
                continue;
            };

            execute_command(command_loader, instance_bundle, &message, request).await;
            continue;
        }

        instance_bundle
            .twitch_irc_client
            .say(message.channel_login.clone(), line.clone())
            .await
            .expect("Failed to send a message");
    }
}

pub async fn handle_stream_event(
    conn: &mut PgConnection,
    instance_bundle: Arc<InstanceBundle>,
    target_id: UserId,
    event_type: EventType,
    parameters: Vec<String>
) {
    let target_id = target_id.take().parse::<i32>().unwrap();
    let events = match ev::events
        .filter(ev::target_alias_id.eq(&target_id))
        .filter(ev::event_type.eq(&event_type))
        .load::<Event>(conn)
    {
        Ok(v) => v,
        Err(e) => {
            println!("[STREAM EVENT HANDLER] Failed to get events: {}", e);
            return;
        }
    };

    for event in events {
        tokio::spawn({
            let instance_bundle = instance_bundle.clone();
            let channel = match ch::channels.find(event.channel_id).first::<Channel>(conn) {
                Ok(v) => v,
                Err(e) => {
                    println!(
                        "[STREAM EVENT HANDLER] Failed to get channel for event ID {}: {}",
                        event.id, e
                    );
                    continue;
                }
            };

            if let Ok(features) = chp::channel_preferences
                .filter(chp::channel_id.eq(&channel.id))
                .select(chp::features)
                .get_result::<Vec<Option<String>>>(conn)
            {
                if features.iter().flatten().any(|x| x.eq(&ChannelFeature::SilentMode.to_string())) {
                    continue;
                }
            }

            let subs = match EventSubscription::belonging_to(&event).load::<EventSubscription>(conn)
            {
                Ok(v) => v,
                Err(e) => {
                    println!(
                        "[STREAM EVENT HANDLER] Failed to get subscriptions for event ID {}: {}",
                        event.id, e
                    );
                    continue;
                }
            };

            let users = match us::users.load::<User>(conn) {
                Ok(v) => v,
                Err(e) => {
                    println!(
                        "[STREAM EVENT HANDLER] Failed to get users for event ID {}: {}",
                        event.id, e
                    );
                    continue;
                }
            };

            let users = users
                .iter()
                .filter(|x| subs.iter().any(|y| y.user_id == x.id))
                .map(|x| format!("@{}", x.alias_name))
                .collect::<Vec<String>>();

            let mut subs: HashSet<String> = HashSet::new();

            subs.extend(users);

            let parameters = parameters.clone();

            async move {
                if event.flags.contains(&EventFlag::Massping) {
                    let broadcaster_id = channel.alias_id.to_string();
                    let moderator_id = instance_bundle.twitch_api_token.user_id.clone().take();

                    let chatters = match instance_bundle.twitch_api_client.req_get(GetChattersRequest::new(broadcaster_id.as_str(), moderator_id.as_str()), &*instance_bundle.twitch_api_token).await {
                        Ok(v) => v,
                        Err(e) => {
                            println!("[STREAM EVENT HANDLER] Failed to get chatters for channel ID {}: {}", channel.id, e);
                            return; 
                        }
                    };

                    let chatters = chatters.data.iter().map(|x| format!("@{}", x.user_login)).collect::<HashSet<String>>();

                    subs.extend(chatters);
                }

                let placeholders = instance_bundle.localizator.parse_placeholders(&event.message);
                let line = instance_bundle.localizator.replace_placeholders(event.message, placeholders, parameters, None);

                if subs.is_empty() {
                    instance_bundle
                        .twitch_irc_client
                        .say(channel.alias_name.clone(), format!("⚡ {}", line))
                        .await
                        .expect("Failed to send a message");
                    return;
                }

                let formatted_subs = split_and_wrap_lines(
                    subs.into_iter()
                        .collect::<Vec<String>>()
                        .join(", ")
                        .as_str(),
                    ", ",
                    300 - line.len(),
                );

                for formatted_sub in formatted_subs {
                    instance_bundle
                        .twitch_irc_client
                        .say(
                            channel.alias_name.clone(),
                            format!("⚡ {} · {}", line, formatted_sub),
                        )
                        .await
                        .expect("Failed to send a message");
                }
            }
        })
        .await
        .unwrap();
    }
}

pub async fn handle_notice_message(instance_bundle: Arc<InstanceBundle>, message: NoticeMessage) {
    if let (Some(msg_id), Some(login)) = (message.message_id, message.channel_login) {
        let conn = &mut establish_connection();

        match msg_id.as_str() {
            "msg_channel_suspended" | "msg_banned" => {
                info!(
                    "#{} will be opted out. Reason: {}.",
                    login,
                    if msg_id.eq("msg_banned") {
                        "The bot has been banned"
                    } else {
                        "The channel has been suspended"
                    }
                );

                instance_bundle.twitch_irc_client.part(login.clone());

                let now = Utc::now().naive_utc();

                let channel =  ch::channels.filter(ch::alias_name.eq(&login)).get_result::<Channel>(conn).expect("Error fetching channel");

                update(ch::channels.find(&channel.id))
                    .set(ch::opt_outed_at.eq(now))
                    .execute(conn)
                    .expect("Failed to update channel after notice message");

                // Parting from channel to stats
                if let Some(stats_password) = &instance_bundle.configuration.third_party.stats_api_password {
                    let client = Client::new();
                    let _ = client
                        .post(format!(
                            "{}/api/users/part",
                            &instance_bundle.configuration.third_party.stats_api_url
                        ))
                        .header("Authorization", format!("Statea {}", stats_password))
                        .multipart(Form::new().text("username", channel.alias_name.clone()))
                        .send()
                        .await;
                }

                if let Ok(features) = chp::channel_preferences.filter(chp::channel_id.eq(&channel.id)).select(chp::features).get_result::<Vec<Option<String>>>(conn) {
                    if features.iter().flatten().any(|x| x.eq(&ChannelFeature::Notify7TVUpdates.to_string())) {
                        let Some(stv_user) = instance_bundle.stv_api_client.get_user_by_twitch_id(channel.alias_id as usize).await else {return;};
                        let mut stv_client = instance_bundle.stv_client.lock().await;
                        stv_client.unsubscribe_emote_set(stv_user.emote_set_id);
                    }
                }
            }
            _ => {}

        }
    }
}

async fn handle_stalk_message_events(conn: &mut PgConnection, instance_bundle: Arc<InstanceBundle>, message: PrivmsgMessage) {
    let id = message.sender.id.parse::<i32>().unwrap();
    let events: Vec<Event> = ev::events.filter(ev::target_alias_id.eq(&id))
        .filter(ev::event_type.eq(&EventType::Message))
        .get_results::<Event>(conn)
        .unwrap_or(Vec::new());

    for event in events {
        tokio::spawn({
            let message = message.clone();
            let instance_bundle = instance_bundle.clone();
            let channel = match ch::channels.find(event.channel_id).first::<Channel>(conn) {
                Ok(v) => v,
                Err(e) => {
                    log::error!("Failed to get channel for event ID {}: {}", event.id, e);
                    return;
                }
            };
            if let Ok(features) = chp::channel_preferences
                .filter(chp::channel_id.eq(&channel.id))
                .select(chp::features)
                .get_result::<Vec<Option<String>>>(conn)
            {
                if features.iter().flatten().any(|x| x.eq(&ChannelFeature::SilentMode.to_string())) {
                    return;
                }
            }

            let subs = match EventSubscription::belonging_to(&event).load::<EventSubscription>(conn)
            {
                Ok(v) => v,
                Err(e) => {
                    log::error!("Failed to get subscriptions for event ID {}: {}", event.id, e);
                    return;
                }
            };

            let users = match us::users.load::<User>(conn) {
                Ok(v) => v,
                Err(e) => {
                    log::error!("Failed to get users for event ID {}: {}", event.id, e);
                    return;
                }
            };

            let users = users
                .iter()
                .filter(|x| subs.iter().any(|y| y.user_id == x.id))
                .map(|x| format!("@{}", x.alias_name))
                .collect::<Vec<String>>();

            let mut subs: HashSet<String> = HashSet::new();

            subs.extend(users);

            async move {
                if event.flags.contains(&EventFlag::Massping) {
                    let broadcaster_id = channel.alias_id.to_string();
                    let moderator_id = instance_bundle.twitch_api_token.user_id.clone().take();

                    let chatters = match instance_bundle.twitch_api_client.req_get(GetChattersRequest::new(broadcaster_id.as_str(), moderator_id.as_str()), &*instance_bundle.twitch_api_token).await {
                        Ok(v) => v,
                        Err(e) => {
                            log::error!("Failed to get chatters for event ID {}: {}", event.id, e);
                            return;
                        }
                    };

                    let chatters = chatters.data.iter().map(|x| format!("@{}", x.user_login)).collect::<HashSet<String>>();

                    subs.extend(chatters);
                }

                let placeholders = instance_bundle.localizator.parse_placeholders(&event.message);
                let line = instance_bundle.localizator.replace_placeholders(event.message, placeholders, vec![
                    message.sender.login.clone(),
                    message.channel_login.clone(),
                    message.message_text.clone()
                ], None);

                if subs.is_empty() {
                    instance_bundle
                        .twitch_irc_client
                        .say(channel.alias_name.clone(), format!("💬 {}", line))
                        .await
                        .expect("Failed to send a message");
                    return;
                }

                let formatted_subs = split_and_wrap_lines(
                    subs.into_iter()
                        .collect::<Vec<String>>()
                        .join(", ")
                        .as_str(),
                    ", ",
                    300 - line.len(),
                );

                for formatted_sub in formatted_subs {
                    instance_bundle
                        .twitch_irc_client
                        .say(
                            channel.alias_name.clone(),
                            format!("💬  {} · {}", line, formatted_sub),
                        )
                        .await
                        .expect("Failed to send a message");
                }
            }
        })
        .await
        .unwrap();
    }
}

async fn execute_command(command_loader: &CommandLoader, instance_bundle: &InstanceBundle, message: &PrivmsgMessage, request: Request) {
    let conn = &mut establish_connection();

    let response = command_loader
            .execute_command(instance_bundle, request.clone())
            .await;

        insert_into(ac::actions)
            .values([NewAction {
                channel_id: request.channel.id,
                user_id: request.sender.id,
                command_name: request.command_id.clone(),
                arguments: match (request.subcommand_id.clone(), request.message.clone()) {
                    (Some(x), Some(y)) => Some(format!("{} {}", x, y)),
                    (Some(x), None) | (None, Some(x)) => Some(x.to_string()),
                    _ => None,
                },
                processed_at: Utc::now().naive_utc(),
                sent_at: message.server_timestamp.naive_utc(),
                response: match response.clone() {
                    Ok(v) => v.to_string(),
                    Err(e) => e.formatted_message(&request, instance_bundle.configuration.third_party.docs_url.clone(), instance_bundle.localizator.clone()),
                },
                status: match response {
                    Ok(_) => common::models::ActionStatus::Ok,
                    Err(_) => common::models::ActionStatus::Error,
                },
            }])
            .execute(conn)
            .expect("Failed to create action log");

        match response {
            Ok(r) => match r {
                Response::Single(line) => {
                    instance_bundle
                        .twitch_irc_client
                        .say(message.channel_login.clone(), line)
                        .await
                        .expect("Failed to send message");
                }
                Response::Multiple(lines) => {
                    for line in lines {
                        instance_bundle
                            .twitch_irc_client
                            .say(message.channel_login.clone(), line)
                            .await
                            .expect("Failed to send message");
                    }
                }
            },
            Err(e) => {
                let response = e.formatted_message(&request, instance_bundle.configuration.third_party.docs_url.clone(), instance_bundle.localizator.clone());

                instance_bundle.twitch_irc_client.say(message.channel_login.clone(), response).await.expect("Failed to send message");
            }
        }
}
