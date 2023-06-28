use crate::schema::*;
use chrono::NaiveDateTime;
use diesel::prelude::*;

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
