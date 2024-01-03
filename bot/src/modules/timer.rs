use async_trait::async_trait;
use diesel::{
    delete, insert_into, update, BelongingToDsl, ExpressionMethods, QueryDsl, RunQueryDsl,
};
use eyre::Result;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command, CommandArgument,
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
        ]
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let subcommand_id = match request.subcommand_id {
            Some(v) => v,
            None => {
                return Err(ResponseError::NotEnoughArguments(
                    CommandArgument::Subcommand,
                ))
            }
        };

        if request.message.is_none() {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Name));
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
            None => return Err(ResponseError::NotEnoughArguments(CommandArgument::Name)),
        };

        let conn = &mut establish_connection();
        let timers = Timer::belonging_to(&request.channel)
            .filter(ti::name.eq(&name_id))
            .load::<Timer>(conn)
            .expect("Failed to load timers when executing 'timer' command");

        let timer = timers.iter().find(|x| x.name.eq(&name_id));

        let response = match (timer, message_split.len(), subcommand_id.as_str()) {
            (Some(t), 0, "delete") => {
                delete(ti::timers.find(&t.id))
                    .execute(conn)
                    .expect(format!("Failed to delete the timer ID {}", t.id.to_string()).as_str());

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::CommandTimerDeleted,
                        vec![t.name.clone(), t.id.to_string()],
                    )
                    .unwrap()
            }
            (Some(t), 0, "toggle") => {
                update(ti::timers.find(&t.id))
                    .set(ti::is_enabled.eq(!t.is_enabled))
                    .execute(conn)
                    .expect(format!("Failed to toggle the timer ID {}", t.id.to_string()).as_str());

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        if !t.is_enabled {
                            LineId::CommandTimerEnabled
                        } else {
                            LineId::CommandTimerDisabled
                        },
                        vec![t.name.clone(), t.id.to_string()],
                    )
                    .unwrap()
            }
            (Some(t), 0, "info") => instance_bundle
                .localizator
                .get_formatted_text(
                    request.channel_preference.language.as_str(),
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
                        t.messages.get(0).clone().unwrap().to_owned(),
                    ],
                )
                .unwrap(),
            (Some(t), 0, "call") => t.messages.get(0).clone().unwrap().to_owned(),

            (Some(t), 1, "interval") => {
                let interval_sec = message_split.get(0).unwrap().to_string();
                let interval_sec = match interval_sec.parse::<u64>() {
                    Ok(v) => v,
                    Err(_) => return Err(ResponseError::IncorrectArgument(interval_sec)),
                } as i64;

                update(ti::timers.find(&t.id))
                    .set(ti::interval_sec.eq(interval_sec))
                    .execute(conn)
                    .expect(
                        format!(
                            "Failed to update the interval for timer ID {}",
                            t.id.to_string()
                        )
                        .as_str(),
                    );

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::CommandTimerInterval,
                        vec![t.name.clone(), t.id.to_string()],
                    )
                    .unwrap()
            }

            (Some(t), _, "message") if !message_split.is_empty() => {
                let message = message_split.join(" ");
                update(ti::timers.find(&t.id))
                    .set(ti::messages.eq(vec![message]))
                    .execute(conn)
                    .expect(
                        format!(
                            "Failed to update the messages for timer ID {}",
                            t.id.to_string()
                        )
                        .as_str(),
                    );

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::CommandTimerMessage,
                        vec![t.name.clone(), t.id.to_string()],
                    )
                    .unwrap()
            }

            (None, _, "new") if message_split.len() > 1 => {
                let interval_sec = message_split.get(0).unwrap().to_string();
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

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::CommandTimerNew,
                        vec![name_id],
                    )
                    .unwrap()
            }

            (Some(_), _, "new") => return Err(ResponseError::NamesakeCreation(name_id)),
            (Some(_), 0, "message") => {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Message))
            }
            (None, _, _) if subcommand_id.ne("new") => {
                return Err(ResponseError::NotFound(name_id))
            }
            (_, 0, _) if subcommand_id.eq("interval") || subcommand_id.eq("new") => {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Interval))
            }
            (None, 1, "new") => {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Message))
            }

            _ => return Err(ResponseError::SomethingWentWrong),
        };

        Ok(Response::Single(response))
    }
}
