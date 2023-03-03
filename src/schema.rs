// @generated automatically by Diesel CLI.

diesel::table! {
    channels (id) {
        id -> Integer,
        alias_id -> Integer,
        referred_from -> Nullable<Integer>,
    }
}

diesel::table! {
    custom_commands (id) {
        id -> Integer,
        channel_id -> Integer,
        name -> Text,
        content -> Text,
        enabled -> Integer,
    }
}

diesel::table! {
    listenable (id) {
        id -> Integer,
        alias_id -> Integer,
        channel_id -> Integer,
        user_ids -> Text,
        message -> Text,
        icon -> Text,
        flags -> Text,
    }
}

diesel::table! {
    preferences (channel_id) {
        channel_id -> Integer,
        flags -> Nullable<Text>,
        prefix -> Nullable<Text>,
        language -> Nullable<Text>,
    }
}

diesel::table! {
    stats (channel_id) {
        channel_id -> Integer,
        chat_lines -> Integer,
        successful_tests -> Integer,
        executed_commands -> Integer,
    }
}

diesel::table! {
    users (id) {
        id -> Integer,
        alias_id -> Integer,
        roles -> Text,
        restrictions -> Text,
        recent_activity -> Text,
        is_suspended -> Integer,
        is_superuser -> Integer,
        secret_key -> Nullable<Text>,
        created_timestamp -> Integer,
        last_timestamp -> Integer,
    }
}

diesel::allow_tables_to_appear_in_same_query!(
    channels,
    custom_commands,
    listenable,
    preferences,
    stats,
    users,
);
