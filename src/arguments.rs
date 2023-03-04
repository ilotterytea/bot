use crate::models::{Channel, NewChannel, NewPreferences, NewUser, Preferences, Stats, User};
use crate::schema::{channels, preferences, stats, users};
use diesel::{insert_into, ExpressionMethods, QueryDsl, RunQueryDsl, SqliteConnection};

pub struct Arguments {
    pub channel: Channel,
    pub preferences: Preferences,
    pub stats: Stats,
    pub user: User,
}

impl Arguments {
    pub fn generate(conn: &mut SqliteConnection, user_id: String, channel_id: String) -> Self {
        let mut channel = channels::dsl::channels
            .filter(channels::dsl::alias_id.eq(channel_id.parse::<i32>().unwrap()))
            .first::<Channel>(conn);

        if channel.is_err() {
            insert_into(channels::dsl::channels)
                .values(vec![NewChannel {
                    alias_id: channel_id.parse::<i32>().unwrap(),
                    referred_from: None,
                    creation_timestamp: i32::try_from(chrono::Utc::now().timestamp()).unwrap(),
                }])
                .execute(conn)
                .expect("Cannot insert the values!");
            channel = channels::dsl::channels
                .filter(channels::dsl::alias_id.eq(channel_id.parse::<i32>().unwrap()))
                .first::<Channel>(conn);
        }

        let _c = channel.unwrap();

        let mut preferences = preferences::dsl::preferences
            .filter(preferences::dsl::channel_id.eq(_c.id))
            .first::<Preferences>(conn);

        if preferences.is_err() {
            insert_into(preferences::dsl::preferences)
                .values(vec![NewPreferences {
                    channel_id: _c.id,
                    flags: None,
                    prefix: None,
                    language: None,
                }])
                .execute(conn)
                .expect("Cannot insert the values!");
            preferences = preferences::dsl::preferences
                .filter(preferences::dsl::channel_id.eq(_c.id))
                .first::<Preferences>(conn);
        }

        let _p = preferences.unwrap();

        let mut stats = stats::dsl::stats
            .filter(stats::dsl::channel_id.eq(_c.id))
            .first::<Stats>(conn);

        if stats.is_err() {
            insert_into(stats::dsl::stats)
                .values(vec![Stats {
                    channel_id: _c.id,
                    chat_lines: 0,
                    successful_tests: 0,
                    executed_commands: 0,
                }])
                .execute(conn)
                .expect("Cannot insert the values!");
            stats = stats::dsl::stats
                .filter(stats::dsl::channel_id.eq(_c.id))
                .first::<Stats>(conn);
        }

        let _s = stats.unwrap();

        let mut user = users::dsl::users
            .filter(users::dsl::alias_id.eq(user_id.parse::<i32>().unwrap()))
            .first::<User>(conn);

        if user.is_err() {
            insert_into(users::dsl::users)
                .values(vec![NewUser {
                    alias_id: user_id.parse::<i32>().unwrap(),
                    created_timestamp: i32::try_from(chrono::Utc::now().timestamp()).unwrap(),
                    last_timestamp: i32::try_from(chrono::Utc::now().timestamp()).unwrap(),
                }])
                .execute(conn)
                .expect("Cannot insert the values!");
            user = users::dsl::users
                .filter(users::dsl::alias_id.eq(user_id.parse::<i32>().unwrap()))
                .first::<User>(conn);
        }

        let _u = user.unwrap();

        Self {
            channel: _c,
            preferences: _p,
            stats: _s,
            user: _u,
        }
    }
}
