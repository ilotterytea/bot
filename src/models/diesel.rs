use crate::schema::*;
use chrono::NaiveDateTime;
use diesel::prelude::*;

#[derive(diesel_derive_enum::DbEnum, Debug, PartialEq)]
#[ExistingTypePath = "crate::schema::sql_types::EventType"]
pub enum EventType {
    Live,
    Offline,
    Title,
    Game,
    Custom,
}

#[derive(diesel_derive_enum::DbEnum, Debug, PartialEq)]
#[ExistingTypePath = "crate::schema::sql_types::EventFlag"]
pub enum EventFlag {
    Massping,
}

#[derive(Queryable)]
pub struct Channel {
    pub id: i32,
    pub alias_id: i32,
    pub alias_name: String,
    pub joined_at: NaiveDateTime,
    pub opt_outed_at: Option<NaiveDateTime>,
}

#[derive(Insertable)]
#[diesel(table_name = channels)]
pub struct NewChannel<'a> {
    pub alias_id: i32,
    pub alias_name: &'a str,
}

#[derive(Queryable)]
pub struct User {
    pub id: i32,
    pub alias_id: i32,
    pub alias_name: String,
    pub joined_at: NaiveDateTime,
    pub is_superuser: bool,
}

#[derive(Insertable)]
#[diesel(table_name = users)]
pub struct NewUser<'a> {
    pub alias_id: i32,
    pub alias_name: &'a str,
}

#[derive(Queryable, Selectable, Identifiable, Associations, Debug, PartialEq)]
#[diesel(belongs_to(Channel))]
pub struct Event {
    pub id: i32,
    pub channel_id: i32,
    pub target_alias_id: Option<i32>,
    pub custom_alias_id: Option<String>,
    pub message: String,
    pub event_type: EventType,
    pub flags: Vec<EventFlag>,
}

#[derive(Insertable)]
#[diesel(table_name = events)]
pub struct NewEvent<'a> {
    pub channel_id: i32,
    pub target_alias_id: Option<i32>,
    pub custom_alias_id: Option<&'a str>,
    pub message: &'a str,
    pub event_type: EventType,
}

#[derive(Queryable, Selectable, Identifiable, Associations, Debug, PartialEq)]
#[diesel(belongs_to(Event))]
#[diesel(belongs_to(User))]
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
