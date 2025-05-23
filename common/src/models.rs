use std::{
    fmt::{Display, Error},
    str::FromStr,
};

use crate::schema::*;
use chrono::NaiveDateTime;
use diesel::{Associations, Identifiable, Insertable, Queryable};
use serde::Serialize;
use uuid::Uuid;

#[derive(Serialize, Queryable, Identifiable, Clone)]
pub struct Channel {
    pub id: i32,
    pub alias_id: i32,
    pub alias_name: String,
    pub joined_at: NaiveDateTime,
    pub opt_outed_at: Option<NaiveDateTime>,
}

#[derive(Insertable)]
#[diesel(table_name = channels)]
pub struct NewChannel {
    pub alias_id: i32,
    pub alias_name: String,
}

#[derive(Serialize, Queryable, Identifiable, Associations, Clone)]
#[diesel(belongs_to(Channel, foreign_key = channel_id))]
#[diesel(table_name = channel_preferences)]
pub struct ChannelPreference {
    pub id: i32,
    pub channel_id: i32,
    pub prefix: String,
    pub language: String,
    pub features: Vec<Option<String>>,
}

#[derive(Insertable)]
#[diesel(table_name = channel_preferences)]
pub struct NewChannelPreference {
    pub channel_id: i32,
    pub prefix: String,
    pub language: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ChannelFeature {
    Notify7TVUpdates,
    NotifyBTTVUpdates,
    SilentMode,
}

impl FromStr for ChannelFeature {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "notify_7tv_updates" => Ok(Self::Notify7TVUpdates),
            "notify_bttv_updates" => Ok(Self::NotifyBTTVUpdates),
            "silent_mode" => Ok(Self::SilentMode),
            _ => Err("failed to serialize a str".to_string()),
        }
    }
}

impl Display for ChannelFeature {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match &self {
            Self::Notify7TVUpdates => write!(f, "notify_7tv_updates"),
            Self::NotifyBTTVUpdates => write!(f, "notify_bttv_updates"),
            Self::SilentMode => write!(f, "silent_mode"),
        }
    }
}

#[derive(Serialize, Identifiable, Queryable, Clone)]
pub struct User {
    pub id: i32,
    pub alias_id: i32,
    pub alias_name: String,
    pub joined_at: NaiveDateTime,
    pub opt_outed_at: Option<NaiveDateTime>,
}

#[derive(Insertable)]
#[diesel(table_name = users)]
pub struct NewUser {
    pub alias_id: i32,
    pub alias_name: String,
}

#[derive(Serialize, diesel_derive_enum::DbEnum, Debug, PartialEq)]
#[ExistingTypePath = "crate::schema::sql_types::ActionStatuses"]
pub enum ActionStatus {
    Ok,
    Error,
}

#[derive(Serialize, Queryable, Identifiable, Associations)]
#[diesel(belongs_to(Channel, foreign_key = channel_id))]
#[diesel(belongs_to(User, foreign_key = user_id))]
#[diesel(table_name = actions)]
pub struct Action {
    pub id: i32,
    pub channel_id: i32,
    pub user_id: i32,
    pub command_name: String,
    pub arguments: Option<String>,
    pub response: String,
    pub status: ActionStatus,
    pub sent_at: NaiveDateTime,
    pub processed_at: NaiveDateTime,
}

#[derive(Insertable)]
#[diesel(table_name = actions)]
pub struct NewAction {
    pub channel_id: i32,
    pub user_id: i32,
    pub command_name: String,
    pub arguments: Option<String>,
    pub response: String,
    pub status: ActionStatus,
    pub sent_at: NaiveDateTime,
    pub processed_at: NaiveDateTime,
}

#[derive(Serialize, Queryable, Identifiable, Associations, Clone)]
#[diesel(belongs_to(Channel, foreign_key = channel_id))]
#[diesel(table_name = timers)]
pub struct Timer {
    pub id: i32,
    pub name: String,
    pub channel_id: i32,
    pub messages: Vec<String>,
    pub interval_sec: i64,
    pub last_executed_at: NaiveDateTime,
    pub is_enabled: bool,
}

#[derive(Insertable)]
#[diesel(table_name = timers)]
pub struct NewTimer {
    pub name: String,
    pub channel_id: i32,
    pub messages: Vec<String>,
    pub interval_sec: i64,
}

#[derive(Serialize, Queryable, Identifiable, Associations, Clone)]
#[diesel(belongs_to(Channel, foreign_key = channel_id))]
#[diesel(table_name = custom_commands)]
pub struct CustomCommand {
    pub id: i32,
    pub channel_id: i32,
    pub name: String,
    pub messages: Vec<String>,
    pub is_enabled: bool,
    pub created_at: NaiveDateTime,
    pub last_executed_at: Option<NaiveDateTime>,
    pub is_global: bool,
}

#[derive(Insertable)]
#[diesel(table_name = custom_commands)]
pub struct NewCustomCommand {
    pub channel_id: i32,
    pub name: String,
    pub messages: Vec<String>,
}

#[derive(Serialize, diesel_derive_enum::DbEnum, Debug, PartialEq, Clone)]
#[ExistingTypePath = "crate::schema::sql_types::EventType"]
pub enum EventType {
    Live,
    Offline,
    Title,
    Category,
    Message,
    Custom,
}

impl FromStr for EventType {
    type Err = Error;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "live" => Ok(Self::Live),
            "offline" => Ok(Self::Offline),
            "title" => Ok(Self::Title),
            "category" => Ok(Self::Category),
            "message" => Ok(Self::Message),
            _ => Ok(Self::Custom),
        }
    }
}

impl ToString for EventType {
    fn to_string(&self) -> String {
        let x = match self {
            Self::Live => "live",
            Self::Offline => "offline",
            Self::Title => "title",
            Self::Category => "category",
            Self::Message => "message",
            Self::Custom => "custom",
        };

        x.to_string()
    }
}

#[derive(Serialize, diesel_derive_enum::DbEnum, Debug, PartialEq, Clone)]
#[ExistingTypePath = "crate::schema::sql_types::EventFlag"]
pub enum EventFlag {
    Massping,
}

impl FromStr for EventFlag {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "massping" => Ok(Self::Massping),
            _ => Err("Failed to parse an event flag".to_string()),
        }
    }
}

impl ToString for EventFlag {
    fn to_string(&self) -> String {
        match self {
            Self::Massping => "massping",
        }
        .to_string()
    }
}

#[derive(Serialize, Queryable, Identifiable, Associations)]
#[diesel(belongs_to(Channel, foreign_key = channel_id))]
pub struct Event {
    pub id: i32,
    pub channel_id: i32,
    pub target_alias_id: Option<i32>,
    pub custom_alias_id: Option<String>,
    pub event_type: EventType,
    pub flags: Vec<EventFlag>,
    pub message: String,
}

#[derive(Insertable)]
#[diesel(table_name = events)]
pub struct NewEvent {
    pub channel_id: i32,
    pub target_alias_id: Option<i32>,
    pub custom_alias_id: Option<String>,
    pub event_type: EventType,
    pub message: String,
}

#[derive(Serialize, Queryable, Identifiable, Associations)]
#[diesel(belongs_to(Event, foreign_key = event_id))]
#[diesel(belongs_to(User, foreign_key = user_id))]
pub struct EventSubscription {
    pub id: i32,
    pub event_id: i32,
    pub user_id: i32,
}

#[derive(Insertable)]
#[diesel(table_name = event_subscriptions)]
pub struct NewEventSubscription {
    pub event_id: i32,
    pub user_id: i32,
}

#[derive(Serialize, diesel_derive_enum::DbEnum, Debug, PartialEq, Clone, Eq, PartialOrd, Ord)]
#[ExistingTypePath = "crate::schema::sql_types::LevelOfRights"]
pub enum LevelOfRights {
    Suspended,
    Subscriber,
    User,
    Vip,
    Moderator,
    Broadcaster,
}

impl LevelOfRights {
    pub fn from_str(s: &str) -> Self {
        match s {
            "suspended" => Self::Suspended,
            "subscriber" => Self::Subscriber,
            "vip" => Self::Vip,
            "moderator" => Self::Moderator,
            "broadcaster" => Self::Broadcaster,
            _ => Self::User,
        }
    }
}

impl Display for LevelOfRights {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Suspended => write!(f, "suspended"),
            Self::Subscriber => write!(f, "subscriber"),
            Self::User => write!(f, "user"),
            Self::Vip => write!(f, "vip"),
            Self::Moderator => write!(f, "moderator"),
            Self::Broadcaster => write!(f, "broadcaster"),
        }
    }
}

#[derive(Serialize, Queryable, Identifiable, Associations, Clone)]
#[diesel(belongs_to(User, foreign_key = user_id))]
#[diesel(belongs_to(Channel, foreign_key = channel_id))]
pub struct Right {
    pub id: i32,
    pub user_id: i32,
    pub channel_id: i32,
    pub level: LevelOfRights,
    pub is_fixed: bool,
}

#[derive(Insertable)]
#[diesel(table_name = rights)]
pub struct NewRight {
    pub user_id: i32,
    pub channel_id: i32,
    pub level: LevelOfRights,
}

#[derive(Serialize, Queryable, Identifiable, Associations)]
#[diesel(belongs_to(User, foreign_key = user_id))]
pub struct Session {
    pub id: i32,
    pub user_id: i32,
    pub access_token: String,
    pub refresh_token: String,
    pub scopes: Vec<Option<String>>,
    pub expires_at: NaiveDateTime,
}

#[derive(Insertable)]
#[diesel(table_name = sessions)]
pub struct NewSession {
    pub user_id: i32,
    pub access_token: String,
    pub refresh_token: String,
    pub scopes: Vec<Option<String>>,
    pub expires_at: NaiveDateTime,
}

#[derive(Serialize, Queryable)]
pub struct SessionState {
    pub state: String,
    pub created_at: NaiveDateTime,
}

#[derive(Insertable)]
#[diesel(table_name = session_states)]
pub struct NewSessionState {
    pub state: String,
}

#[derive(Queryable)]
pub struct UserToken {
    pub user_id: i32,
    pub token: Uuid,
    pub created_at: NaiveDateTime,
}

#[derive(Insertable)]
#[diesel(table_name = user_tokens)]
pub struct NewUserToken {
    pub user_id: i32,
}

#[derive(Queryable)]
pub struct LuaUserStorage {
    pub id: i32,
    pub user_id: i32,
    pub lua_id: String,
    pub value: String,
}

#[derive(Insertable)]
#[diesel(table_name = lua_user_storage)]
pub struct NewLuaUserStorage {
    pub user_id: i32,
    pub lua_id: String,
}

#[derive(Queryable)]
pub struct LuaChannelStorage {
    pub id: i32,
    pub channel_id: i32,
    pub lua_id: String,
    pub value: String,
}

#[derive(Insertable)]
#[diesel(table_name = lua_channel_storage)]
pub struct NewLuaChannelStorage {
    pub channel_id: i32,
    pub lua_id: String,
}
