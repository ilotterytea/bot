use diesel::{insert_into, BelongingToDsl, ExpressionMethods, QueryDsl, RunQueryDsl};
use twitch_irc::message::PrivmsgMessage;

use crate::{
    command::CommandLoader,
    instance_bundle::InstanceBundle,
    message::ParsedPrivmsgMessage,
    models::diesel::{Channel, ChannelPreference, NewChannel, NewChannelPreference, NewUser, User},
    schema::{channel_preferences::dsl as chp, channels::dsl as ch, users::dsl as us},
    shared_variables::{DEFAULT_LANGUAGE, DEFAULT_PREFIX},
    utils::diesel::{create_action, establish_connection},
};

pub async fn handle_chat_message(
    instance_bundle: InstanceBundle,
    command_loader: &CommandLoader,
    message: PrivmsgMessage,
) {
    let parsed_message =
        ParsedPrivmsgMessage::parse(message.message_text.as_str(), '~', command_loader);
    let conn = &mut establish_connection();

    let channel = ch::channels
        .filter(ch::alias_id.eq(message.channel_id.parse::<i32>().unwrap()))
        .first::<Channel>(conn)
        .unwrap_or_else(|_| {
            insert_into(ch::channels)
                .values(vec![NewChannel {
                    alias_id: message.channel_id.parse::<i32>().unwrap(),
                }])
                .execute(conn)
                .expect("Failed to create a new channel");

            ch::channels
                .filter(ch::alias_id.eq(message.channel_id.parse::<i32>().unwrap()))
                .first::<Channel>(conn)
                .expect("Failed to get a channel after creating it")
        });

    let mut channel_preference = ChannelPreference::belonging_to(&channel)
        .first::<ChannelPreference>(conn)
        .unwrap_or_else(|_| {
            insert_into(chp::channel_preferences)
                .values(vec![NewChannelPreference {
                    channel_id: channel.id,
                }])
                .execute(conn)
                .expect("Failed to create preferences for channel");

            ChannelPreference::belonging_to(&channel)
                .first::<ChannelPreference>(conn)
                .expect("Failed to get preferences after creating them")
        });

    if let None = channel_preference.prefix {
        channel_preference.prefix = Some(DEFAULT_PREFIX.to_string());
    }

    if let None = channel_preference.language {
        channel_preference.language = Some(DEFAULT_LANGUAGE.to_string());
    }

    let user = us::users
        .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
        .first::<User>(conn)
        .unwrap_or_else(|_| {
            insert_into(us::users)
                .values(vec![NewUser {
                    alias_id: message.sender.id.parse::<i32>().unwrap(),
                }])
                .execute(conn)
                .expect("Failed to create a new user");

            us::users
                .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
                .first::<User>(conn)
                .expect("Failed to get a user after creating it")
        });

    if let Some(parsed_message) = parsed_message {
        if let Ok(Some(response)) = command_loader
            .execute_command(
                &instance_bundle,
                message.clone(),
                parsed_message.clone(),
                &channel,
                &channel_preference,
                &user,
            )
            .await
        {
            create_action(
                conn,
                &parsed_message,
                Some(response.join("\n")),
                channel.id,
                user.id,
            );

            for line in response {
                instance_bundle
                    .twitch_irc_client
                    .say(message.channel_login.clone(), line)
                    .await
                    .expect("Failed to send message");
            }
        }
    }
}
