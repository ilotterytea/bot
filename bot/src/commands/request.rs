use chrono::{NaiveDateTime, Utc};
use diesel::{
    insert_into, update, BelongingToDsl, ExpressionMethods, PgConnection, QueryDsl, RunQueryDsl,
};
use mlua::{Lua, Table};
use substring::Substring;
use twitch_irc::message::PrivmsgMessage;

use common::{
    config::CommandsConfiguration,
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
        config: &CommandsConfiguration,
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
                        prefix: config.default_prefix.clone(),
                        language: config.default_language.clone(),
                    }])
                    .execute(conn)
                    .expect("Failed to create preferences for channel");

                ChannelPreference::belonging_to(&channel)
                    .first::<ChannelPreference>(conn)
                    .expect("Failed to get preferences after creating them")
            });

        let mut sender = us::users
            .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
            .first::<User>(conn)
            .unwrap_or_else(|_| {
                insert_into(us::users)
                    .values(vec![NewUser {
                        alias_id: message.sender.id.parse::<i32>().unwrap(),
                        alias_name: message.sender.login.clone(),
                    }])
                    .execute(conn)
                    .expect("Failed to create a new user");

                us::users
                    .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
                    .first::<User>(conn)
                    .expect("Failed to get a user after creating it")
            });

        if sender.alias_name.ne(&message.sender.login) {
            sender.alias_name = message.sender.login.clone();

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
                .rust_commands
                .iter()
                .any(|x| x.get_name().eq(&word))
                || command_loader.lua_commands.iter().any(|x| x.name.eq(&word))
            {
                word
            } else {
                return None;
            }
        } else {
            return None;
        };

        let command_rights: LevelOfRights;
        let command_delay: i32;
        let command_subcommands: Vec<String>;

        match (
            command_loader
                .rust_commands
                .iter()
                .find(|x| x.get_name().eq(&command_id)),
            command_loader
                .lua_commands
                .iter()
                .find(|x| x.name.eq(&command_id)),
        ) {
            (Some(c), _) => {
                command_rights = c.required_rights().clone();
                command_delay = c.get_delay_sec();
                command_subcommands = c.get_subcommands().clone();
            }
            (_, Some(c)) => {
                command_rights = c.minimal_rights.clone();
                command_delay = c.delay_sec as i32;
                command_subcommands = c.subcommands.clone();
            }
            _ => return None,
        }

        if command_rights > rights.level {
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

            if now_timestamp - la_timestamp < command_delay as i64 {
                return None;
            }
        }

        message_split.remove(0);

        let subcommand_id = if let Some(v) = message_split.first() {
            let v = v.to_string();

            if command_subcommands.contains(&v) {
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

    pub fn as_lua_table(&self, lua: &Lua) -> mlua::Result<Table> {
        let table = lua.create_table()?;

        // metadata
        table.set("command_id", self.command_id.clone())?;
        table.set("subcommand_id", self.subcommand_id.clone())?;
        table.set("message", self.message.clone())?;

        // sender
        let sender_table = lua.create_table()?;
        sender_table.set("id", self.sender.id)?;
        sender_table.set("alias_id", self.sender.alias_id)?;
        sender_table.set("alias_name", self.sender.alias_name.clone())?;
        sender_table.set("joined_at", self.sender.joined_at.timestamp_millis())?;
        sender_table.set(
            "opted_out_at",
            if let Some(datetime) = &self.sender.opt_outed_at {
                Some(datetime.timestamp_millis())
            } else {
                None
            },
        )?;
        table.set("sender", sender_table)?;

        // channel
        let channel_table = lua.create_table()?;
        channel_table.set("id", self.channel.id)?;
        channel_table.set("alias_id", self.channel.alias_id)?;
        channel_table.set("alias_name", self.channel.alias_name.clone())?;
        channel_table.set("joined_at", self.channel.joined_at.timestamp_millis())?;
        channel_table.set(
            "opted_out_at",
            if let Some(datetime) = &self.channel.opt_outed_at {
                Some(datetime.timestamp_millis())
            } else {
                None
            },
        )?;
        table.set("channel", channel_table)?;

        // channel preference
        let channel_preference_table = lua.create_table()?;
        channel_preference_table.set("id", self.channel_preference.id)?;
        channel_preference_table.set("channel_id", self.channel_preference.channel_id)?;
        channel_preference_table.set("prefix", self.channel_preference.prefix.clone())?;
        channel_preference_table.set("language", self.channel_preference.language.clone())?;
        channel_preference_table.set("features", self.channel_preference.features.clone())?;
        table.set("channel_preference", channel_preference_table)?;

        // rights
        let rights_table = lua.create_table()?;
        rights_table.set("id", self.rights.id)?;
        rights_table.set("user_id", self.rights.user_id)?;
        rights_table.set("channel_id", self.rights.channel_id)?;
        rights_table.set("level", self.rights.level.to_string())?;
        rights_table.set("is_fixed", self.rights.is_fixed)?;
        table.set("rights", rights_table)?;

        Ok(table)
    }

    pub fn from_lua_table(table: Table) -> mlua::Result<Self> {
        // metadata
        let command_id: String = table.get("command_id")?;
        let subcommand_id: Option<String> = table.get("subcommand_id")?;
        let message: Option<String> = table.get("message")?;

        // sender
        let sender: Table = table.get("sender")?;
        let sender = User {
            id: sender.get("id")?,
            alias_id: sender.get("alias_id")?,
            alias_name: sender.get("alias_name")?,
            joined_at: NaiveDateTime::from_timestamp_millis(sender.get("joined_at")?)
                .expect("Error parsing NaiveDateTime"),
            opt_outed_at: if let Ok(timestamp) = sender.get::<i64>("opted_out_at") {
                Some(
                    NaiveDateTime::from_timestamp_millis(timestamp)
                        .expect("Error parsing NaiveDateTime"),
                )
            } else {
                None
            },
        };

        // channel
        let channel: Table = table.get("channel")?;
        let channel = Channel {
            id: channel.get("id")?,
            alias_id: channel.get("alias_id")?,
            alias_name: channel.get("alias_name")?,
            joined_at: NaiveDateTime::from_timestamp_millis(channel.get("joined_at")?)
                .expect("Error parsing NaiveDateTime"),
            opt_outed_at: if let Ok(timestamp) = channel.get::<i64>("opted_out_at") {
                Some(
                    NaiveDateTime::from_timestamp_millis(timestamp)
                        .expect("Error parsing NaiveDateTime"),
                )
            } else {
                None
            },
        };

        // channel preference
        let channel_preference: Table = table.get("channel_preference")?;
        let channel_preference = ChannelPreference {
            id: channel_preference.get("id")?,
            channel_id: channel_preference.get("channel_id")?,
            prefix: channel_preference.get("prefix")?,
            language: channel_preference.get("language")?,
            features: channel_preference
                .get::<Table>("features")?
                .sequence_values::<String>()
                .map(|x| x.ok())
                .collect::<Vec<Option<String>>>(),
        };

        // rights
        let rights: Table = table.get("rights")?;
        let rights = Right {
            id: rights.get("id")?,
            user_id: rights.get("user_id")?,
            channel_id: rights.get("channel_id")?,
            level: LevelOfRights::from_str(&rights.get::<String>("level")?),
            is_fixed: rights.get("is_fixed")?,
        };

        Ok(Request {
            command_id,
            subcommand_id,
            message,
            sender,
            channel,
            channel_preference,
            rights,
        })
    }
}
