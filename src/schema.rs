// @generated automatically by Diesel CLI.

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

diesel::joinable!(channel_preferences -> channels (channel_id));

diesel::allow_tables_to_appear_in_same_query!(
    channel_preferences,
    channels,
    users,
);
