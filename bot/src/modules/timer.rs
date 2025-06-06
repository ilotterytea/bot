use async_trait::async_trait;
use diesel::{
    BelongingToDsl, ExpressionMethods, QueryDsl, RunQueryDsl, delete, insert_into, update,
};

use crate::{
    commands::{
        Command, CommandArgument,
        request::Request,
        response::{Response, ResponseError},
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
};

use common::{
    establish_connection,
    models::{LevelOfRights, NewTimer, Timer},
    schema::timers::dsl as ti,
};

pub struct TimerCommand;

#[async_trait]
impl Command for TimerCommand {
    fn get_name(&self) -> String {
        "timer".to_string()
    }

    fn required_rights(&self) -> LevelOfRights {
        LevelOfRights::Moderator
    }

    fn get_subcommands(&self) -> Vec<String> {
        vec![
            "new".to_string(),
            "delete".to_string(),
            "message".to_string(),
            "interval".to_string(),
            "toggle".to_string(),
            "info".to_string(),
            "call".to_string(),
            "list".to_string(),
        ]
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let subcommand_id = match request.subcommand_id.clone() {
            Some(v) => v,
            None => {
                return Err(ResponseError::NotEnoughArguments(
                    CommandArgument::Subcommand,
                ));
            }
        };

        let conn = &mut establish_connection();

        if subcommand_id == "list" {
            let timers: Vec<Timer> = Timer::belonging_to(&request.channel)
                .get_results(conn)
                .expect("Failed to get timers");

            if timers.is_empty() {
                return Ok(Response::Single(
                    instance_bundle.localizator.formatted_text_by_request(
                        &request,
                        LineId::TimerListEmpty,
                        Vec::<String>::new(),
                    ),
                ));
            }

            let timer_names = timers
                .iter()
                .map(|x| x.name.to_string())
                .collect::<Vec<String>>();

            return Ok(Response::Single(
                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::TimerList,
                    vec![timer_names.join(", ")],
                ),
            ));
        }

        if request.message.is_none() {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Name));
        }
        let message = request.message.clone().unwrap();
        let mut message_split = message.split(' ').collect::<Vec<&str>>();

        // Subcommands that requires one argument only
        let name_id = match message_split.first() {
            Some(v) => {
                let v = v.to_string();
                message_split.remove(0);
                v
            }
            None => return Err(ResponseError::NotEnoughArguments(CommandArgument::Name)),
        };

        let timers = Timer::belonging_to(&request.channel)
            .filter(ti::name.eq(&name_id))
            .load::<Timer>(conn)
            .expect("Failed to load timers when executing 'timer' command");

        let timer = timers.iter().find(|x| x.name.eq(&name_id));

        let response = match (timer, message_split.len(), subcommand_id.as_str()) {
            (Some(t), 0, "delete") => {
                delete(ti::timers.find(&t.id))
                    .execute(conn)
                    .unwrap_or_else(|_| panic!("Failed to delete the timer ID {}", t.id));

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandTimerDeleted,
                    vec![t.name.clone(), t.id.to_string()],
                )
            }
            (Some(t), 0, "toggle") => {
                update(ti::timers.find(&t.id))
                    .set(ti::is_enabled.eq(!t.is_enabled))
                    .execute(conn)
                    .unwrap_or_else(|_| panic!("Failed to toggle the timer ID {}", t.id));

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    if !t.is_enabled {
                        LineId::CommandTimerEnabled
                    } else {
                        LineId::CommandTimerDisabled
                    },
                    vec![t.name.clone(), t.id.to_string()],
                )
            }
            (Some(t), 0, "info") => instance_bundle.localizator.formatted_text_by_request(
                &request,
                LineId::CommandTimerInfo,
                vec![
                    if t.is_enabled {
                        "✅".to_string()
                    } else {
                        "❌".to_string()
                    },
                    t.name.clone(),
                    t.id.to_string(),
                    t.interval_sec.to_string(),
                    t.messages.first().unwrap().to_owned(),
                ],
            ),
            (Some(t), 0, "call") => t.messages.first().unwrap().to_owned(),

            (Some(t), 1, "interval") => {
                let interval_sec = message_split.first().unwrap().to_string();
                let interval_sec = match interval_sec.parse::<u64>() {
                    Ok(v) => v,
                    Err(_) => return Err(ResponseError::IncorrectArgument(interval_sec)),
                } as i64;

                update(ti::timers.find(&t.id))
                    .set(ti::interval_sec.eq(interval_sec))
                    .execute(conn)
                    .unwrap_or_else(|_| {
                        panic!("Failed to update the interval for timer ID {}", t.id)
                    });

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandTimerInterval,
                    vec![t.name.clone(), t.id.to_string()],
                )
            }

            (Some(t), _, "message") if !message_split.is_empty() => {
                let message = message_split.join(" ");
                update(ti::timers.find(&t.id))
                    .set(ti::messages.eq(vec![message]))
                    .execute(conn)
                    .unwrap_or_else(|_| {
                        panic!("Failed to update the messages for timer ID {}", t.id)
                    });

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandTimerMessage,
                    vec![t.name.clone(), t.id.to_string()],
                )
            }

            (None, _, "new") if message_split.len() > 1 => {
                let interval_sec = message_split.first().unwrap().to_string();
                message_split.remove(0);

                let interval_sec = match interval_sec.parse::<u64>() {
                    Ok(v) => v,
                    Err(_) => return Err(ResponseError::IncorrectArgument(interval_sec)),
                } as i64;

                let message = message_split.join(" ");

                insert_into(ti::timers)
                    .values([NewTimer {
                        name: name_id.clone(),
                        channel_id: request.channel.id,
                        messages: vec![message],
                        interval_sec,
                    }])
                    .execute(conn)
                    .expect("Failed to insert a new timer");

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandTimerNew,
                    vec![name_id],
                )
            }

            (Some(_), _, "new") => return Err(ResponseError::NamesakeCreation(name_id)),
            (Some(_), 0, "message") => {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Message));
            }
            (None, _, _) if subcommand_id.ne("new") => {
                return Err(ResponseError::NotFound(name_id));
            }
            (_, 0, _) if subcommand_id.eq("interval") || subcommand_id.eq("new") => {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Interval));
            }
            (None, 1, "new") => {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Message));
            }

            _ => return Err(ResponseError::SomethingWentWrong),
        };

        Ok(Response::Single(response))
    }
}
