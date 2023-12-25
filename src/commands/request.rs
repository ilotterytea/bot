use diesel::{insert_into, BelongingToDsl, ExpressionMethods, PgConnection, QueryDsl, RunQueryDsl};
use substring::Substring;
use twitch_irc::message::PrivmsgMessage;

use crate::{
    models::diesel::{Channel, ChannelPreference, NewChannel, NewChannelPreference, NewUser, User},
    schema::{channel_preferences::dsl as chp, channels::dsl as ch, users::dsl as us},
    shared_variables::{DEFAULT_LANGUAGE, DEFAULT_PREFIX},
};

use super::CommandLoader;

#[derive(Clone)]
pub struct Request {
    pub command_id: String,
    pub subcommand_id: Option<String>,
    pub message: Option<String>,

    pub sender: User,
    pub channel: Channel,
    pub channel_preference: ChannelPreference,
}

impl Request {
    pub fn try_from(
        message: &PrivmsgMessage,
        command_loader: &CommandLoader,
        conn: &mut PgConnection,
    ) -> Option<Request> {
        let channel = ch::channels
            .filter(ch::alias_id.eq(message.channel_id.parse::<i32>().unwrap()))
            .first::<Channel>(conn)
            .unwrap_or_else(|_| {
                insert_into(ch::channels)
                    .values(vec![NewChannel {
                        alias_id: message.channel_id.parse::<i32>().unwrap(),
                        alias_name: message.channel_login.clone(),
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
                        prefix: DEFAULT_PREFIX.to_string(),
                        language: DEFAULT_LANGUAGE.to_string(),
                    }])
                    .execute(conn)
                    .expect("Failed to create preferences for channel");

                ChannelPreference::belonging_to(&channel)
                    .first::<ChannelPreference>(conn)
                    .expect("Failed to get preferences after creating them")
            });

        let sender = us::users
            .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
            .first::<User>(conn)
            .unwrap_or_else(|_| {
                insert_into(us::users)
                    .values(vec![NewUser {
                        alias_id: message.sender.id.parse::<i32>().unwrap(),
                        alias_name: message.sender.name.clone(),
                    }])
                    .execute(conn)
                    .expect("Failed to create a new user");

                us::users
                    .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
                    .first::<User>(conn)
                    .expect("Failed to get a user after creating it")
            });

        let prefix = channel_preference.prefix.as_str();

        if !message.message_text.starts_with(prefix) {
            return None;
        }

        let message = message
            .message_text
            .substring(prefix.len(), message.message_text.len())
            .to_string();
        let mut message_split = message.split(' ').collect::<Vec<&str>>();

        let command_id = if let Some(word) = message_split.get(0) {
            let word = word.to_string();
            if command_loader
                .commands
                .iter()
                .find(|x| x.get_name().eq(&word))
                .is_some()
            {
                word
            } else {
                return None;
            }
        } else {
            return None;
        };

        let command = command_loader
            .commands
            .iter()
            .find(|x| x.get_name().eq(&command_id))
            .unwrap();

        message_split.remove(0);

        let subcommand_id = if let Some(v) = message_split.get(0) {
            let v = v.to_string();

            if command.get_subcommands().contains(&v) {
                message_split.remove(0);
                Some(v)
            } else {
                None
            }
        } else {
            None
        };

        Some(Request {
            command_id,
            subcommand_id,
            message: if message_split.is_empty() {
                None
            } else {
                Some(message_split.join(" "))
            },
            sender,
            channel,
            channel_preference,
        })
    }
}
