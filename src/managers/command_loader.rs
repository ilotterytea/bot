use diesel::{insert_into, update, ExpressionMethods, QueryDsl, RunQueryDsl, SqliteConnection};

use crate::commands::command::{CommandBehavior, CommandData};

use crate::builtin_commands::ping::Ping;
use crate::models::{NewUser, RecentActivity, User};

pub struct CommandLoader {
    commands: Vec<CommandData>,
}

impl CommandLoader {
    pub fn new() -> Self {
        let mut commands: Vec<CommandData> = vec![];

        commands.push(Ping::new().0);

        CommandLoader { commands }
    }
    pub fn run(
        &self,
        conn: &mut SqliteConnection,
        id: &str,
        user_id: &str,
        channel_id: &str,
    ) -> Option<String> {
        let mut response: Option<String> = None;

        for cid in &self.commands {
            if (&cid).id.eq(id) {
                if self.is_delayed(
                    conn,
                    id,
                    user_id,
                    channel_id,
                    i32::try_from(cid.delay).unwrap(),
                ) {
                    break;
                }

                response = ((&cid).run)();
                self.delay(conn, id, user_id, channel_id);
            }
        }

        response
    }
    pub fn delay(
        &self,
        conn: &mut SqliteConnection,
        command_id: &str,
        user_id: &str,
        channel_id: &str,
    ) -> bool {
        use crate::schema::users::dsl::*;

        let mut result = users
            .filter(alias_id.eq(user_id.parse::<i32>().unwrap()))
            .first::<User>(conn);

        if result.is_err() {
            insert_into(users)
                .values(vec![NewUser {
                    alias_id: user_id.parse::<i32>().unwrap(),
                    created_timestamp: i32::try_from(chrono::Utc::now().timestamp()).unwrap(),
                    last_timestamp: i32::try_from(chrono::Utc::now().timestamp()).unwrap(),
                }])
                .execute(conn)
                .expect("Cannot insert values!");
            result = users
                .filter(alias_id.eq(user_id.parse::<i32>().unwrap()))
                .first::<User>(conn);
        }
        let _rs = result.unwrap();

        let mut delayed: Vec<RecentActivity> =
            serde_json::from_str(_rs.recent_activity.as_str()).unwrap();

        let d = delayed.iter().find(|e| {
            e.command_id.eq(&command_id) && e.channel_id.eq(&channel_id.parse::<i32>().unwrap())
        });

        if d.is_some() {
            return true;
        }

        delayed.push(RecentActivity {
            command_id: command_id.to_string(),
            channel_id: channel_id.parse::<i32>().unwrap(),
            timestamp: i32::try_from(chrono::Utc::now().timestamp()).unwrap(),
        });

        update(users.filter(alias_id.eq(user_id.parse::<i32>().unwrap())))
            .set(recent_activity.eq(serde_json::to_string(&delayed).unwrap()))
            .execute(conn)
            .expect("Cannot update the values!");

        true
    }
    pub fn is_delayed(
        &self,
        conn: &mut SqliteConnection,
        command_id: &str,
        user_id: &str,
        channel_id: &str,
        delay_ms: i32,
    ) -> bool {
        use crate::schema::users::dsl::*;

        let mut result = users
            .filter(alias_id.eq(user_id.parse::<i32>().unwrap()))
            .first::<User>(conn);

        if result.is_err() {
            insert_into(users)
                .values(vec![NewUser {
                    alias_id: user_id.parse::<i32>().unwrap(),
                    created_timestamp: i32::try_from(chrono::Utc::now().timestamp()).unwrap(),
                    last_timestamp: i32::try_from(chrono::Utc::now().timestamp()).unwrap(),
                }])
                .execute(conn)
                .expect("Cannot insert values!");
            result = users
                .filter(alias_id.eq(user_id.parse::<i32>().unwrap()))
                .first::<User>(conn);
        }
        let _rs = result.unwrap();

        let mut delayed: Vec<RecentActivity> =
            serde_json::from_str(_rs.recent_activity.as_str()).unwrap();

        let d = delayed.iter().find(|e| {
            e.command_id.eq(&command_id) && e.channel_id.eq(&channel_id.parse::<i32>().unwrap())
        });

        if d.is_some() {
            let _d = d.unwrap();
            let index = delayed.iter().position(|e| e.eq(_d)).unwrap();

            if i32::try_from(chrono::Utc::now().timestamp()).unwrap() - _d.timestamp > delay_ms {
                delayed.remove(index);
            } else {
                return true;
            }
        } else {
            return false;
        }

        update(users.filter(alias_id.eq(user_id.parse::<i32>().unwrap())))
            .set(recent_activity.eq(serde_json::to_string(&delayed).unwrap()))
            .execute(conn)
            .expect("Cannot update the values!");

        false
    }
    pub fn get_loaded_commands(&self) -> &Vec<CommandData> {
        &self.commands
    }
}
