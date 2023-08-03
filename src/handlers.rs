use crate::api::command::CommandLoader;
use crate::api::message::ParsedMessage;
use crate::api::InstanceBundle;
use crate::models::diesel::{
    Channel, Event, EventFlag, EventSubscription, EventType, NewChannel, NewUser, User,
};
use crate::schema::{channels::dsl as ch, users::dsl as us};
use crate::utils::{establish_connection, split_and_wrap_lines};
use diesel::{insert_into, ExpressionMethods, QueryDsl, RunQueryDsl};
use itertools::Itertools;
use std::collections::HashSet;
use std::sync::Arc;
use tokio::sync::MutexGuard;
use twitch_api::helix::chat::GetChattersRequest;
use twitch_api::twitch_oauth2::UserToken;
use twitch_api::types::{Nickname, UserId};
use twitch_api::HelixClient;
use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::PrivmsgMessage;
use twitch_irc::{SecureTCPTransport, TwitchIRCClient};

/// The handler for Twitch IRC messages.
pub async fn irc_message_handler(
    instance_bundle: InstanceBundle,
    command_loader: MutexGuard<'_, CommandLoader>,
    message: PrivmsgMessage,
) {
    println!("Received message: {:?}", message);

    let wrapped_parsed_msg = ParsedMessage::parse(&command_loader, &message.message_text);
    let conn = &mut establish_connection();

    // Getting the channel or creating if somehow it doesn't exist
    let mut channel = ch::channels
        .filter(ch::alias_id.eq(message.channel_id.parse::<i32>().unwrap()))
        .first::<Channel>(conn)
        .unwrap_or_else(|_| {
            insert_into(ch::channels)
                .values(vec![NewChannel {
                    alias_id: message.channel_id.parse::<i32>().unwrap(),
                    alias_name: message.channel_login.as_str(),
                }])
                .execute(conn)
                .expect("Failed to create a new channel");

            let c = ch::channels
                .filter(ch::alias_id.eq(message.channel_id.parse::<i32>().unwrap()))
                .first::<Channel>(conn)
                .expect("Failed to get a channel after creating it");

            c
        });

    // Update the channel data
    channel.alias_name = message.channel_login.clone();

    // Getting the sender or creating if user is new for the bot
    let mut user = us::users
        .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
        .first::<User>(conn)
        .unwrap_or_else(|_| {
            insert_into(us::users)
                .values(vec![NewUser {
                    alias_id: message.sender.id.parse::<i32>().unwrap(),
                    alias_name: message.sender.login.as_str(),
                }])
                .execute(conn)
                .expect("Failed to create a new user");

            let u = us::users
                .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
                .first::<User>(conn)
                .expect("Failed to get a user even after creating it");

            u
        });

    if wrapped_parsed_msg.is_some() {
        let parsed_msg = wrapped_parsed_msg.unwrap();

        let command = command_loader
            .run(&instance_bundle, channel, user, parsed_msg)
            .await;

        if command.is_some() {
            for line in command.unwrap() {
                instance_bundle
                    .twitch_client
                    .say(message.channel_login.to_owned(), line)
                    .await
                    .expect("Couldn't send the message!")
            }
        }
    }
}

pub async fn handle_stream_event(
    client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    api_client: Arc<HelixClient<'static, reqwest::Client>>,
    api_token: Arc<UserToken>,
    channel_id: UserId,
    event_type: EventType,
) {
    use crate::schema::{event_subscriptions::dsl as sub, events::dsl as ev};
    let conn = &mut establish_connection();

    let events = match ev::events
        .filter(ev::target_alias_id.eq(channel_id.take().parse::<i32>().unwrap()))
        .filter(ev::event_type.eq(&event_type))
        .load::<Event>(conn)
    {
        Ok(e) => e,
        Err(e) => {
            println!("Failed to get events: {}", e);
            return;
        }
    };

    for event in events {
        tokio::spawn({
            let conn = &mut establish_connection();
            let client = Arc::clone(&client);
            let api_client = Arc::clone(&api_client);
            let api_token = Arc::clone(&api_token);
            let channel = ch::channels
                .find(event.channel_id)
                .first::<Channel>(conn)
                .expect("Failed to get a channel");

            async move {
                let conn = &mut establish_connection();
                let mut subs: HashSet<String> = HashSet::new();

                if event.flags.contains(&Some(EventFlag::Massping)) {
                    let broadcaster_id = channel.alias_id.to_string();
                    let moderator_id = api_token.user_id.clone().take();
                    let chatters = match api_client
                        .req_get(
                            GetChattersRequest::new(broadcaster_id.as_str(), moderator_id.as_str()),
                            &*api_token,
                        )
                        .await
                    {
                        Ok(response) => response,
                        Err(e) => {
                            println!(
                                "Failed to get chatters for channel ID {}: {}",
                                broadcaster_id, e
                            );
                            return;
                        }
                    };

                    let data = chatters
                        .data
                        .iter()
                        .map(|x| format!("@{}", x.user_login))
                        .collect::<HashSet<String>>();

                    subs.extend(data);
                }

                let sub_data = sub::event_subscriptions
                    .filter(sub::event_id.eq(&event.id))
                    .load::<EventSubscription>(conn)
                    .expect("Failed to load subscribers of event");

                for data in sub_data {
                    let user = us::users
                        .find(data.user_id)
                        .first::<User>(conn)
                        .expect("Failed to find a user");

                    subs.insert(format!("@{}", user.alias_name));
                }

                if subs.is_empty() {
                    client
                        .say(channel.alias_name.into(), format!("⚡ {}", event.message))
                        .await
                        .expect("Failed to send a message");

                    return;
                }

                let formatted_subs = split_and_wrap_lines(
                    subs.iter().join(", ").as_str(),
                    ", ",
                    300 - event.message.len(),
                );

                for formatted_sub in formatted_subs {
                    client
                        .say(
                            channel.alias_name.clone().into(),
                            format!("⚡{} · {}", event.message.clone(), formatted_sub),
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
