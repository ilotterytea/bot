use std::env;

use diesel::{insert_into, Connection, PgConnection, RunQueryDsl};

use crate::{
    message::ParsedPrivmsgMessage, models::diesel::NewAction, schema::actions::dsl as ac,
    shared_variables::DEFAULT_PREFIX,
};

pub fn establish_connection() -> PgConnection {
    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
    PgConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Error connecting to {}", database_url))
}

pub fn create_action(
    conn: &mut PgConnection,
    parsed_message: &ParsedPrivmsgMessage,
    response: Option<String>,
    channel_id: i32,
    user_id: i32,
) {
    insert_into(ac::actions)
        .values([NewAction {
            command: parsed_message.command_id.clone(),
            full_message: format!(
                "{}{}{}",
                DEFAULT_PREFIX,
                parsed_message.command_id,
                if parsed_message.message.is_some() {
                    format!(" {}", parsed_message.message.clone().unwrap())
                } else {
                    "".to_string()
                }
            ),
            attributes: parsed_message.message.clone(),
            response,
            channel_id,
            user_id,
        }])
        .execute(conn)
        .expect("Failed to insert an action");
}
