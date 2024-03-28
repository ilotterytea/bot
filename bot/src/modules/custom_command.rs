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
    models::{CustomCommand, LevelOfRights, NewCustomCommand},
    schema::custom_commands::dsl as cc,
};

pub struct CustomCommandsCommand;

#[async_trait]
impl Command for CustomCommandsCommand {
    fn get_name(&self) -> String {
        "cmd".to_string()
    }

    fn required_rights(&self) -> LevelOfRights {
        LevelOfRights::Moderator
    }

    fn get_subcommands(&self) -> Vec<String> {
        vec![
            "new".to_string(),
            "delete".to_string(),
            "message".to_string(),
            "toggle".to_string(),
            "info".to_string(),
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
                ))
            }
        };

        let conn = &mut establish_connection();

        if subcommand_id == "list" {
            let cmds: Vec<CustomCommand> = CustomCommand::belonging_to(&request.channel)
                .get_results(conn)
                .expect("Failed to get custom commands");

            if cmds.is_empty() {
                return Ok(Response::Single(
                    instance_bundle.localizator.formatted_text_by_request(
                        &request,
                        LineId::CustomcommandListEmpty,
                        Vec::<String>::new(),
                    ),
                ));
            }

            let cmd_names = cmds
                .iter()
                .map(|x| format!("{}", x.name))
                .collect::<Vec<String>>();

            return Ok(Response::Single(
                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CustomcommandList,
                    vec![cmd_names.join(", ")],
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

        let commands = CustomCommand::belonging_to(&request.channel)
            .filter(cc::name.eq(&name_id))
            .load::<CustomCommand>(conn)
            .expect("Failed to load custom commands when executing 'cmd' command");

        let command = commands.iter().find(|x| x.name.eq(&name_id));

        let response = match (command, message_split.len(), subcommand_id.as_str()) {
            (Some(c), 0, "delete") => {
                delete(cc::custom_commands.find(&c.id))
                    .execute(conn)
                    .unwrap_or_else(|_| panic!("Failed to delete the custom command ID {}", c.id));

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandCustomCommandDeleted,
                    vec![c.name.clone(), c.id.to_string()],
                )
            }
            (Some(c), 0, "toggle") => {
                update(cc::custom_commands.find(&c.id))
                    .set(cc::is_enabled.eq(!c.is_enabled))
                    .execute(conn)
                    .unwrap_or_else(|_| panic!("Failed to toggle the custom command ID {}", c.id));

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    if !c.is_enabled {
                        LineId::CommandCustomCommandEnabled
                    } else {
                        LineId::CommandCustomCommandDisabled
                    },
                    vec![c.name.clone(), c.id.to_string()],
                )
            }
            (Some(c), 0, "info") => instance_bundle.localizator.formatted_text_by_request(
                &request,
                LineId::CommandCustomCommandInfo,
                vec![
                    if c.is_enabled {
                        "✅".to_string()
                    } else {
                        "❌".to_string()
                    },
                    c.name.clone(),
                    c.id.to_string(),
                    c.messages.first().unwrap().to_owned(),
                ],
            ),
            (Some(c), _, "message") if !message_split.is_empty() => {
                let message = message_split.join(" ");
                update(cc::custom_commands.find(&c.id))
                    .set(cc::messages.eq(vec![message]))
                    .execute(conn)
                    .unwrap_or_else(|_| {
                        panic!(
                            "Failed to update the messages for custom command ID {}",
                            c.id
                        )
                    });

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandCustomCommandMessage,
                    vec![c.name.clone(), c.id.to_string()],
                )
            }

            (None, _, "new") if !message_split.is_empty() => {
                let message = message_split.join(" ");

                insert_into(cc::custom_commands)
                    .values([NewCustomCommand {
                        name: name_id.clone(),
                        channel_id: request.channel.id,
                        messages: vec![message],
                    }])
                    .execute(conn)
                    .expect("Failed to insert a new custom command");

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandCustomCommandNew,
                    vec![name_id],
                )
            }

            (None, 0, _) if subcommand_id.ne("new") => {
                return Err(ResponseError::NotFound(name_id))
            }

            (None, 0, "new") => {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Message))
            }

            (Some(_), _, "new") => return Err(ResponseError::NamesakeCreation(name_id)),

            _ => return Err(ResponseError::SomethingWentWrong),
        };

        Ok(Response::Single(response))
    }
}
