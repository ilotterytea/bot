// @generated automatically by Diesel CLI.

diesel::table! {
    actions (id) {
        id -> Int4,
        channel_id -> Int4,
        user_id -> Int4,
        command -> Varchar,
        attributes -> Nullable<Varchar>,
        full_message -> Varchar,
        response -> Nullable<Varchar>,
        timestamp -> Timestamp,
    }
}

diesel::table! {
    channel_preferences (id) {
        id -> Int4,
        channel_id -> Int4,
        prefix -> Nullable<Varchar>,
        language -> Nullable<Varchar>,
    }
}

diesel::table! {
    channels (id) {
        id -> Int4,
        alias_id -> Int4,
        joined_at -> Timestamp,
        opt_outed_at -> Nullable<Timestamp>,
    }
}

diesel::table! {
    users (id) {
        id -> Int4,
        alias_id -> Int4,
        joined_at -> Timestamp,
        opt_outed_at -> Nullable<Timestamp>,
        is_super_user -> Bool,
    }
}

diesel::joinable!(actions -> channels (channel_id));
diesel::joinable!(actions -> users (user_id));
diesel::joinable!(channel_preferences -> channels (channel_id));

diesel::allow_tables_to_appear_in_same_query!(
    actions,
    channel_preferences,
    channels,
    users,
);
