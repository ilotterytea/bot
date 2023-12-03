use crate::schema::*;
use chrono::NaiveDateTime;
use diesel::{Associations, Identifiable, Insertable, Queryable};

#[derive(Queryable, Identifiable, Clone)]
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

#[derive(Queryable, Identifiable, Associations, Clone)]
#[diesel(belongs_to(Channel, foreign_key = channel_id))]
#[diesel(table_name = channel_preferences)]
pub struct ChannelPreference {
    pub id: i32,
    pub channel_id: i32,
    pub prefix: String,
    pub language: String,
}

#[derive(Insertable)]
#[diesel(table_name = channel_preferences)]
pub struct NewChannelPreference {
    pub channel_id: i32,
    pub prefix: String,
    pub language: String,
}

#[derive(Queryable, Clone)]
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

#[derive(diesel_derive_enum::DbEnum, Debug, PartialEq)]
#[ExistingTypePath = "crate::schema::sql_types::ActionStatuses"]
pub enum ActionStatus {
    Ok,
    Error,
}

#[derive(Queryable, Identifiable, Associations)]
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
