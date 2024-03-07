use std::{collections::HashSet, sync::Arc};

use chrono::Utc;
use diesel::{update, BelongingToDsl, ExpressionMethods, PgConnection, QueryDsl, RunQueryDsl};
use twitch_api::{types::UserId, helix::chat::GetChattersRequest};
use twitch_irc::message::PrivmsgMessage;

use crate::{
    commands::{request::Request, response::Response, CommandLoader},
    instance_bundle::InstanceBundle,
    message::ParsedPrivmsgMessage,
    
    
    utils::{
        diesel::{create_action},
        split_and_wrap_lines,
    },
};

use common::{
    models::{
        Channel, CustomCommand, Event, EventFlag, EventSubscription, EventType, Timer, User,
    },
    schema::{
        channels::dsl as ch, custom_commands::dsl as cc, event_subscriptions::dsl as evs,
        events::dsl as ev, timers::dsl as ti, users::dsl as us,
    },
    establish_connection
};

pub async fn handle_chat_message(
    instance_bundle: Arc<InstanceBundle>,
    command_loader: &CommandLoader,
    message: PrivmsgMessage,
) {
    let conn = &mut establish_connection();

    if let Some(request) = Request::try_from(&message, command_loader, conn) {
        let response = command_loader
            .execute_command(&instance_bundle, request.clone())
            .await;

        // TODO: CREATE ACTION LOG LATER

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
                let response = e.formatted_message(&request, instance_bundle.localizator.clone());

                instance_bundle.twitch_irc_client.say(message.channel_login.clone(), response).await.expect("Failed to send message");
            }
        }
    }

    handle_custom_commands(conn, &instance_bundle, &message).await;
}

pub async fn handle_timers(instance_bundle: &InstanceBundle) {
    let current_timestamp = Utc::now().naive_utc();
    let conn = &mut establish_connection();
    let channels = ch::channels
        .filter(ch::opt_outed_at.is_null())
        .load::<Channel>(conn)
        .expect("Failed to get channels");

    for channel in channels {
        let timers = Timer::belonging_to(&channel)
            .filter(ti::is_enabled.eq(true))
            .load::<Timer>(conn)
            .expect("Failed to get timers for channel ID ");

        for timer in timers {
            if current_timestamp.timestamp() - timer.last_executed_at.timestamp()
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
) {
    let message_text = message.message_text.clone();

    let alias_id = message.channel_id.parse::<i32>().unwrap();
    let channels = ch::channels
        .filter(ch::alias_id.eq(&alias_id))
        .load::<Channel>(conn)
        .expect(format!("Failed to load channel data with alias ID {}", alias_id).as_str());

    if let Some(channel) = channels.first() {
        let commands = CustomCommand::belonging_to(&channel)
            .filter(cc::is_enabled.eq(true))
            .load::<CustomCommand>(conn)
            .expect("Failed to load custom commands");

        if let Some(command) = commands.iter().find(|x| x.name.eq(&message_text)) {
            for line in &command.messages {
                instance_bundle
                    .twitch_irc_client
                    .say(message.channel_login.clone(), line.clone())
                    .await
                    .expect("Failed to send a message");
            }
        }
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

                if subs.is_empty() {
                    instance_bundle
                        .twitch_irc_client
                        .say(channel.alias_name.clone(), format!("⚡ {}", event.message))
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
                    300 - event.message.len(),
                );


                let placeholders = instance_bundle.localizator.parse_placeholders(&event.message);
                let line = instance_bundle.localizator.replace_placeholders(event.message, placeholders, parameters, None);

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
