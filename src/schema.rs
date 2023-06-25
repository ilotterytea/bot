// @generated automatically by Diesel CLI.

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
    users (id) {
        id -> Int4,
        alias_id -> Int4,
        #[max_length = 25]
        alias_name -> Varchar,
        joined_at -> Timestamp,
        is_superuser -> Bool,
    }
}

diesel::allow_tables_to_appear_in_same_query!(
    channels,
    users,
);
