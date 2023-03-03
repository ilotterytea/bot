use crate::schema::*;
use diesel::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Queryable)]
pub struct Channel {
    pub id: i32,
    pub alias_id: i32,
    pub referred_from: Option<i32>,
    pub creation_timestamp: i32,
}

#[derive(Insertable)]
#[diesel(table_name = channels)]
pub struct NewChannel {
    pub alias_id: i32,
    pub referred_from: Option<i32>,
    pub creation_timestamp: i32,
}

#[derive(Queryable)]
pub struct Preferences {
    pub channel_id: i32,
    pub flags: Option<String>,
    pub prefix: Option<String>,
    pub language: Option<String>,
}

#[derive(Insertable)]
#[diesel(table_name = preferences)]
pub struct NewPreferences<'a> {
    pub channel_id: i32,
    pub flags: Option<&'a str>,
    pub prefix: Option<&'a str>,
    pub language: Option<&'a str>,
}

#[derive(Queryable, Insertable)]
#[diesel(table_name = stats)]
pub struct Stats {
    pub channel_id: i32,
    pub chat_lines: i32,
    pub successful_tests: i32,
    pub executed_commands: i32,
}

#[derive(Queryable)]
pub struct CustomCommand {
    pub id: i32,
    pub channel_id: i32,
    pub name: String,
    pub content: String,
    pub enabled: i32,
}

#[derive(Insertable)]
#[diesel(table_name = custom_commands)]
pub struct NewCustomCommand<'a> {
    pub id: i32,
    pub channel_id: i32,
    pub name: &'a str,
    pub content: &'a str,
    pub enabled: i32,
}

#[derive(Queryable)]
pub struct User {
    pub id: i32,
    pub alias_id: i32,
    pub roles: String,
    pub restrictions: String,
    pub recent_activity: String,
    pub is_suspended: i32,
    pub is_superuser: i32,
    pub secret_key: Option<String>,
    pub created_timestamp: i32,
    pub last_timestamp: i32,
}

#[derive(Insertable)]
#[diesel(table_name = users)]
pub struct NewUser {
    pub alias_id: i32,
    pub created_timestamp: i32,
    pub last_timestamp: i32,
}

#[derive(Serialize, Deserialize, PartialEq, Eq)]
pub struct RecentActivity {
    pub command_id: String,
    pub channel_id: i32,
    pub timestamp: i32,
}

#[derive(Queryable)]
pub struct Listenable {
    pub id: i32,
    pub alias_id: i32,
    pub channel_id: i32,
    pub message: String,
    pub icon: String,
    pub flags: String,
}

#[derive(Insertable)]
#[diesel(table_name = listenable)]
pub struct NewListenable {
    pub alias_id: i32,
    pub channel_id: i32,
}

#[derive(Queryable)]
pub struct Subscriber {
    pub id: i32,
    pub user_id: i32,
    pub listenable_id: i32,
    pub events: String,
    pub enabled: i32,
}

#[derive(Insertable)]
#[diesel(table_name = subscribers)]
pub struct NewSubscriber {
    pub user_id: i32,
    pub listenable_id: i32,
}
