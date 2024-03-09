use std::env;

use chrono::{NaiveDateTime, Utc};
use diesel::{
    insert_into, update, BelongingToDsl, ExpressionMethods, PgConnection, QueryDsl, RunQueryDsl,
};
use substring::Substring;
use twitch_irc::message::PrivmsgMessage;

use crate::shared_variables::{DEFAULT_LANGUAGE, DEFAULT_PREFIX};

use common::{
    models::{
        Channel, ChannelPreference, LevelOfRights, NewChannel, NewChannelPreference, NewRight,
        NewUser, Right, User,
    },
    schema::{
        actions::dsl as ac, channel_preferences::dsl as chp, channels::dsl as ch,
        rights::dsl as ri, users::dsl as us,
    },
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
    pub rights: Right,
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

        let channel_preference = ChannelPreference::belonging_to(&channel)
            .first::<ChannelPreference>(conn)
            .unwrap_or_else(|_| {
                insert_into(chp::channel_preferences)
                    .values(vec![NewChannelPreference {
                        channel_id: channel.id,
                        prefix: match env::var("BOT_DEFAULT_PREFIX") {
                            Ok(v) => v,
                            Err(_) => DEFAULT_PREFIX.to_string(),
                        },
                        language: match env::var("BOT_DEFAULT_LANGUAGE") {
                            Ok(v) => v,
                            Err(_) => DEFAULT_LANGUAGE.to_string(),
                        },
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

        if sender.alias_name.ne(&message.sender.login) {
            update(us::users.find(sender.id))
                .set(us::alias_name.eq(&message.sender.login))
                .execute(conn)
                .expect("Failed to update user name");
        }

        let mut badges_iter = message.badges.iter();

        let level_of_rights = if sender.alias_id == channel.alias_id {
            LevelOfRights::Broadcaster
        } else if badges_iter.any(|x| x.name.eq("moderator")) {
            LevelOfRights::Moderator
        } else if badges_iter.any(|x| x.name.eq("vip")) {
            LevelOfRights::Vip
        } else if badges_iter.any(|x| x.name.eq("subscriber")) {
            LevelOfRights::Subscriber
        } else {
            LevelOfRights::User
        };

        let mut rights = ri::rights
            .filter(ri::user_id.eq(&sender.id))
            .get_result::<Right>(conn)
            .unwrap_or_else(|_| {
                insert_into(ri::rights)
                    .values([NewRight {
                        user_id: sender.id,
                        channel_id: channel.id,
                        level: level_of_rights.clone(),
                    }])
                    .get_result::<Right>(conn)
                    .expect("Failed to insert a new rights")
            });

        if rights.level != LevelOfRights::Suspended && rights.level != level_of_rights {
            rights.level = level_of_rights.clone();

            update(ri::rights.find(&rights.id))
                .set(ri::level.eq(level_of_rights))
                .execute(conn)
                .expect("Failed to update rights");
        }

        let prefix = channel_preference.prefix.as_str();

        if !message.message_text.starts_with(prefix) {
            return None;
        }

        let message = message
            .message_text
            .substring(prefix.len(), message.message_text.len())
            .to_string();
        let mut message_split = message.split(' ').collect::<Vec<&str>>();

        let command_id = if let Some(word) = message_split.first() {
            let word = word.to_string();
            if command_loader
                .commands
                .iter()
                .any(|x| x.get_name().eq(&word))
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

        if command.required_rights() > rights.level {
            return None;
        }

        let last_action_timestamp = ac::actions
            .filter(ac::channel_id.eq(&channel.id))
            .filter(ac::user_id.eq(&sender.id))
            .filter(ac::command_name.eq(&command_id))
            .select(ac::processed_at)
            .order(ac::processed_at.desc())
            .first::<NaiveDateTime>(conn);

        if let Ok(last_action_timestamp) = last_action_timestamp {
            let la_timestamp: i64 = last_action_timestamp.timestamp();
            let now_timestamp: i64 = Utc::now().naive_utc().timestamp();

            if now_timestamp - la_timestamp < command.get_delay_sec() as i64 {
                return None;
            }
        }

        message_split.remove(0);

        let subcommand_id = if let Some(v) = message_split.first() {
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
            rights,
        })
    }
}
