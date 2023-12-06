// @generated automatically by Diesel CLI.

pub mod sql_types {
    #[derive(diesel::query_builder::QueryId, diesel::sql_types::SqlType)]
    #[diesel(postgres_type(name = "action_statuses"))]
    pub struct ActionStatuses;
}

diesel::table! {
    use diesel::sql_types::*;
    use super::sql_types::ActionStatuses;

    actions (id) {
        id -> Int4,
        user_id -> Int4,
        channel_id -> Int4,
        command_name -> Varchar,
        arguments -> Nullable<Varchar>,
        response -> Varchar,
        status -> ActionStatuses,
        sent_at -> Timestamp,
        processed_at -> Timestamp,
    }
}

diesel::table! {
    channel_preferences (id) {
        id -> Int4,
        channel_id -> Int4,
        prefix -> Varchar,
        language -> Varchar,
    }
}

diesel::table! {
    channels (id) {
        id -> Int4,
        alias_id -> Int4,
        alias_name -> Varchar,
        joined_at -> Timestamp,
        opt_outed_at -> Nullable<Timestamp>,
    }
}

diesel::table! {
    timers (id) {
        id -> Int4,
        name -> Varchar,
        channel_id -> Int4,
        messages -> Array<Text>,
        interval_sec -> Int8,
        last_executed_at -> Timestamp,
        is_enabled -> Bool,
    }
}

diesel::table! {
    users (id) {
        id -> Int4,
        alias_id -> Int4,
        alias_name -> Varchar,
        joined_at -> Timestamp,
        opt_outed_at -> Nullable<Timestamp>,
    }
}

diesel::joinable!(actions -> channels (channel_id));
diesel::joinable!(actions -> users (user_id));
diesel::joinable!(channel_preferences -> channels (channel_id));
diesel::joinable!(timers -> channels (channel_id));

diesel::allow_tables_to_appear_in_same_query!(
    actions,
    channel_preferences,
    channels,
    timers,
    users,
);
