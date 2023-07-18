// @generated automatically by Diesel CLI.

pub mod sql_types {
    #[derive(diesel::query_builder::QueryId, diesel::sql_types::SqlType)]
    #[diesel(postgres_type(name = "event_flag"))]
    pub struct EventFlag;

    #[derive(diesel::query_builder::QueryId, diesel::sql_types::SqlType)]
    #[diesel(postgres_type(name = "event_type"))]
    pub struct EventType;
}

diesel::table! {
    channels (id) {
        id -> Int4,
        alias_id -> Int4,
        #[max_length = 25]
        alias_name -> Varchar,
        joined_at -> Timestamp,
        opt_outed_at -> Nullable<Timestamp>,
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
        #[max_length = 500]
        custom_alias_id -> Nullable<Varchar>,
        #[max_length = 500]
        message -> Varchar,
        event_type -> EventType,
        flags -> Array<Nullable<EventFlag>>,
    }
}

diesel::table! {
    users (id) {
        id -> Int4,
        alias_id -> Int4,
        #[max_length = 25]
        alias_name -> Varchar,
        joined_at -> Timestamp,
        is_superuser -> Bool,
    }
}

diesel::joinable!(event_subscriptions -> events (event_id));
diesel::joinable!(event_subscriptions -> users (user_id));
diesel::joinable!(events -> channels (channel_id));

diesel::allow_tables_to_appear_in_same_query!(
    channels,
    event_subscriptions,
    events,
    users,
);
