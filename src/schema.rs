// @generated automatically by Diesel CLI.

diesel::table! {
    channels (id) {
        id -> Integer,
        alias_id -> Integer,
        referred_from -> Nullable<Integer>,
        creation_timestamp -> Integer,
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
        messages -> Text,
        icons -> Text,
        flags -> Text,
        enabled -> Integer,
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
    subscribers (id) {
        id -> Integer,
        user_id -> Integer,
        listenable_id -> Integer,
        events -> Text,
        enabled -> Integer,
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
    subscribers,
    users,
);
