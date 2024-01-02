use std::env;

use chrono::{NaiveDateTime, Utc};
use diesel::{insert_into, Connection, PgConnection, RunQueryDsl};

use crate::message::ParsedPrivmsgMessage;

use common::{
    models::{ActionStatus, NewAction},
    schema::actions::dsl as ac,
};

pub fn establish_connection() -> PgConnection {
    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
    PgConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Error connecting to {}", database_url))
}

pub fn create_action(
    conn: &mut PgConnection,
    parsed_message: &ParsedPrivmsgMessage,
    response: String,
    channel_id: i32,
    user_id: i32,
    sent_at: NaiveDateTime,
    status: ActionStatus,
) {
    insert_into(ac::actions)
        .values([NewAction {
            command_name: parsed_message.command_id.clone(),
            arguments: parsed_message.message.clone(),
            response,
            channel_id,
            user_id,
            sent_at,
            status,
            processed_at: Utc::now().naive_utc(),
        }])
        .execute(conn)
        .expect("Failed to insert an action");
}
