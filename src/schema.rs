// @generated automatically by Diesel CLI.

pub mod sql_types {
    #[derive(diesel::query_builder::QueryId, diesel::sql_types::SqlType)]
    #[diesel(postgres_type(name = "action_statuses"))]
    pub struct ActionStatuses;

    #[derive(diesel::query_builder::QueryId, diesel::sql_types::SqlType)]
    #[diesel(postgres_type(name = "event_flag"))]
    pub struct EventFlag;

    #[derive(diesel::query_builder::QueryId, diesel::sql_types::SqlType)]
    #[diesel(postgres_type(name = "event_type"))]
    pub struct EventType;
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
    custom_commands (id) {
        id -> Int4,
        channel_id -> Int4,
        name -> Varchar,
        messages -> Array<Text>,
        is_enabled -> Bool,
        created_at -> Timestamp,
        last_executed_at -> Nullable<Timestamp>,
    }
}

diesel::table! {
    event_subscriptions (id) {
        id -> Int4,
        event_id -> Int4,
        user_id -> Int4,
    }
}

diesel::table! {
    use diesel::sql_types::*;
    use super::sql_types::EventType;
    use super::sql_types::EventFlag;

    events (id) {
        id -> Int4,
        channel_id -> Int4,
        target_alias_id -> Nullable<Int4>,
        custom_alias_id -> Nullable<Varchar>,
        event_type -> EventType,
        flags -> Array<EventFlag>,
        message -> Varchar,
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
diesel::joinable!(custom_commands -> channels (channel_id));
diesel::joinable!(event_subscriptions -> events (event_id));
diesel::joinable!(event_subscriptions -> users (user_id));
diesel::joinable!(events -> channels (channel_id));
diesel::joinable!(timers -> channels (channel_id));

diesel::allow_tables_to_appear_in_same_query!(
    actions,
    channel_preferences,
    channels,
    custom_commands,
    event_subscriptions,
    events,
    timers,
    users,
);
