use chrono::Utc;
use diesel::{update, BelongingToDsl, ExpressionMethods, QueryDsl, RunQueryDsl};
use twitch_irc::message::PrivmsgMessage;

use crate::{
    commands::{request::Request, response::Response, CommandLoader},
    instance_bundle::InstanceBundle,
    message::ParsedPrivmsgMessage,
    models::diesel::{Channel, Timer},
    schema::{channels::dsl as ch, timers::dsl as ti},
    utils::diesel::{create_action, establish_connection},
};

pub async fn handle_chat_message(
    instance_bundle: InstanceBundle,
    command_loader: &CommandLoader,
    message: PrivmsgMessage,
) {
    let conn = &mut establish_connection();

    if let Some(request) = Request::try_from(&message, "~", command_loader, conn) {
        let response = command_loader
            .execute_command(&instance_bundle, request.clone())
            .await;

        // TODO: CREATE ACTION LOG LATER

        match response {
            Ok(r) => match r {
                Response::Single(line) => {
                    instance_bundle
                        .twitch_irc_client
                        .say(message.channel_login.clone(), line)
                        .await
                        .expect("Failed to send message");
                }
                Response::Multiple(lines) => {
                    for line in lines {
                        instance_bundle
                            .twitch_irc_client
                            .say(message.channel_login.clone(), line)
                            .await
                            .expect("Failed to send message");
                    }
                }
            },
            Err(_) => {}
        }
    }
}

pub async fn handle_timers(instance_bundle: &InstanceBundle) {
    let current_timestamp = Utc::now().naive_utc();
    let conn = &mut establish_connection();
    let channels = ch::channels
        .filter(ch::opt_outed_at.is_null())
        .load::<Channel>(conn)
        .expect("Failed to get channels");

    for channel in channels {
        let timers = Timer::belonging_to(&channel)
            .filter(ti::is_enabled.eq(true))
            .load::<Timer>(conn)
            .expect("Failed to get timers for channel ID ");

        for timer in timers {
            if current_timestamp.timestamp() - timer.last_executed_at.timestamp()
                < timer.interval_sec
            {
                continue;
            }

            for line in timer.messages {
                instance_bundle
                    .twitch_irc_client
                    .say(channel.alias_name.clone(), line)
                    .await
                    .expect("Failed to send a message");
            }

            update(ti::timers.filter(ti::id.eq(timer.id)))
                .set(ti::last_executed_at.eq(current_timestamp))
                .execute(conn)
                .expect("Failed to update the timer with ID");
        }
    }
}
