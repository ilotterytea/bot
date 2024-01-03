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
        let commands = CustomCommand::belonging_to(&request.channel)
            .filter(cc::name.eq(&name_id))
            .load::<CustomCommand>(conn)
            .expect("Failed to load custom commands when executing 'cmd' command");

        let command = commands.iter().find(|x| x.name.eq(&name_id));

        let response = match (command, message_split.len(), subcommand_id.as_str()) {
            (Some(c), 0, "delete") => {
                delete(cc::custom_commands.find(&c.id))
                    .execute(conn)
                    .expect(
                        format!(
                            "Failed to delete the custom command ID {}",
                            c.id.to_string()
                        )
                        .as_str(),
                    );

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::CommandCustomCommandDeleted,
                        vec![c.name.clone(), c.id.to_string()],
                    )
                    .unwrap()
            }
            (Some(c), 0, "toggle") => {
                update(cc::custom_commands.find(&c.id))
                    .set(cc::is_enabled.eq(!c.is_enabled))
                    .execute(conn)
                    .expect(
                        format!(
                            "Failed to toggle the custom command ID {}",
                            c.id.to_string()
                        )
                        .as_str(),
                    );

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        if !c.is_enabled {
                            LineId::CommandCustomCommandEnabled
                        } else {
                            LineId::CommandCustomCommandDisabled
                        },
                        vec![c.name.clone(), c.id.to_string()],
                    )
                    .unwrap()
            }
            (Some(c), 0, "info") => instance_bundle
                .localizator
                .get_formatted_text(
                    request.channel_preference.language.as_str(),
                    LineId::CommandCustomCommandInfo,
                    vec![
                        if c.is_enabled {
                            "✅".to_string()
                        } else {
                            "❌".to_string()
                        },
                        c.name.clone(),
                        c.id.to_string(),
                        c.messages.get(0).clone().unwrap().to_owned(),
                    ],
                )
                .unwrap(),
            (Some(c), _, "message") if !message_split.is_empty() => {
                let message = message_split.join(" ");
                update(cc::custom_commands.find(&c.id))
                    .set(cc::messages.eq(vec![message]))
                    .execute(conn)
                    .expect(
                        format!(
                            "Failed to update the messages for custom command ID {}",
                            c.id.to_string()
                        )
                        .as_str(),
                    );

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::CommandCustomCommandMessage,
                        vec![c.name.clone(), c.id.to_string()],
                    )
                    .unwrap()
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

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::CommandCustomCommandNew,
                        vec![name_id],
                    )
                    .unwrap()
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
