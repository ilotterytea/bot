use async_trait::async_trait;
use diesel::{delete, update, BelongingToDsl, ExpressionMethods, QueryDsl, RunQueryDsl};
use eyre::Result;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    models::diesel::Timer,
    schema::timers::dsl as ti,
    shared_variables::START_TIME,
    utils::{diesel::establish_connection, format_timestamp},
};

pub struct TimerCommand;

#[async_trait]
impl Command for TimerCommand {
    fn get_name(&self) -> String {
        "timer".to_string()
    }

    fn get_subcommands(&self) -> Vec<String> {
        vec![
            "new".to_string(),
            "delete".to_string(),
            "edit".to_string(),
            "interval".to_string(),
            "toggle".to_string(),
            "info".to_string(),
            "call".to_string(),
        ]
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let subcommand_id = match request.subcommand_id {
            Some(v) => v,
            None => return Err(ResponseError::NoSubcommand),
        };

        if request.message.is_none() {
            return Err(ResponseError::NoMessage);
        }
        let message = request.message.unwrap();
        let mut message_split = message.split(" ").collect::<Vec<&str>>();

        // Subcommands that requires one argument only
        let name_id = match message_split.get(0) {
            Some(v) => {
                let v = v.to_string();
                message_split.remove(0);
                v
            }
            None => return Err(ResponseError::NotEnoughArguments),
        };

        let mut response: String = "".into();

        let conn = &mut establish_connection();
        let timers = Timer::belonging_to(&request.channel)
            .filter(ti::name.eq(&name_id))
            .load::<Timer>(conn)
            .expect("Failed to load timers when executing 'timer' command");

        let timer = timers.iter().find(|x| x.name.eq(&name_id));

        if timer.is_some() {
            let timer = timer.unwrap();

            if message_split.len() == 0 {
                match subcommand_id.as_str() {
                    "delete" => {
                        delete(ti::timers.find(&timer.id))
                            .execute(conn)
                            .expect("Failed to delete timer ID ");

                        response = instance_bundle
                            .localizator
                            .get_formatted_text(
                                request.channel_preference.language.as_str(),
                                LineId::CommandTimerDeleted,
                                vec![timer.name.clone(), timer.id.to_string()],
                            )
                            .unwrap();
                    }
                    "toggle" => {
                        update(ti::timers.find(&timer.id))
                            .set(ti::is_enabled.eq(!timer.is_enabled))
                            .execute(conn)
                            .expect("Failed to toggle timer ID ");

                        response = instance_bundle
                            .localizator
                            .get_formatted_text(
                                request.channel_preference.language.as_str(),
                                if !timer.is_enabled {
                                    LineId::CommandTimerDisabled
                                } else {
                                    LineId::CommandTimerEnabled
                                },
                                vec![timer.name.clone(), timer.id.to_string()],
                            )
                            .unwrap();
                    }
                    "info" => {
                        response = instance_bundle
                            .localizator
                            .get_formatted_text(
                                request.channel_preference.language.as_str(),
                                LineId::CommandTimerInfo,
                                vec![
                                    timer.name.clone(),
                                    timer.id.to_string(),
                                    timer.interval_sec.to_string(),
                                    if timer.is_enabled {
                                        "✅".to_string()
                                    } else {
                                        "❌".to_string()
                                    },
                                    timer.messages.get(0).unwrap().clone(),
                                ],
                            )
                            .unwrap();
                    }
                    "call" => {
                        response = timer.messages.get(0).unwrap().clone();
                    }
                    _ => {}
                }
            } else if message_split.len() == 1 {
                let second_arg = message_split.get(0).unwrap().to_string();
                message_split.remove(0);

                match subcommand_id.as_str() {
                    "interval" => {
                        let interval = match second_arg.parse::<i64>() {
                            Ok(v) => v,
                            Err(_) => return Err(ResponseError::NotEnoughArguments),
                        };

                        update(ti::timers.find(&timer.id))
                            .set(ti::interval_sec.eq(interval))
                            .execute(conn)
                            .expect("Failed to update the interval of timer ID ");

                        response = instance_bundle
                            .localizator
                            .get_formatted_text(
                                request.channel_preference.language.as_str(),
                                LineId::CommandTimerInterval,
                                vec![
                                    timer.name.clone(),
                                    timer.id.to_string(),
                                    timer.interval_sec.to_string(),
                                    if timer.is_enabled {
                                        "✅".to_string()
                                    } else {
                                        "❌".to_string()
                                    },
                                    timer.messages.get(0).unwrap().clone(),
                                ],
                            )
                            .unwrap();
                    }
                    _ => {}
                }
            } else {
                let second_arg = message_split.get(0).unwrap().to_string();
                message_split.remove(0);

                if message_split.is_empty() {
                    return Err(ResponseError::NoMessage);
                }

                let third_arg = message_split.join(" ");

                match subcommand_id.as_str() {
                    "edit" => {}
                    _ => {}
                }
            }
        }

        Ok(Response::Single(response))
    }
}
