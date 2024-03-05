use async_trait::async_trait;
use diesel::{update, ExpressionMethods, RunQueryDsl};
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
    establish_connection, models::LevelOfRights, schema::channel_preferences::dsl as chp,
};

pub struct SettingsCommand;

#[async_trait]
impl Command for SettingsCommand {
    fn get_name(&self) -> String {
        "set".to_string()
    }

    fn required_rights(&self) -> LevelOfRights {
        LevelOfRights::Broadcaster
    }

    fn get_subcommands(&self) -> Vec<String> {
        vec!["locale".to_string(), "prefix".to_string()]
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        mut request: Request,
    ) -> Result<Response, ResponseError> {
        let subcommand_id = match request.subcommand_id.clone() {
            Some(v) => v,
            None => {
                return Err(ResponseError::NotEnoughArguments(
                    CommandArgument::Subcommand,
                ))
            }
        };

        if request.message.is_none() {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Value));
        }

        let message = request.message.clone().unwrap();

        let conn = &mut establish_connection();

        let response = match subcommand_id.as_str() {
            "locale" => {
                let locales = instance_bundle.localizator.localization_names();

                if !locales.contains(&&message) {
                    return Err(ResponseError::NotFound(message));
                }

                request.channel_preference.language = message.clone();

                update(chp::channel_preferences)
                    .set(chp::language.eq(message))
                    .execute(conn)
                    .expect("Failed to update the channel preference");

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::SettingsLocale,
                    Vec::<String>::new(),
                )
            }
            "prefix" => {
                update(chp::channel_preferences)
                    .set(chp::prefix.eq(message.clone()))
                    .execute(conn)
                    .expect("Failed to update the channel preference");

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::SettingsPrefix,
                    vec![message],
                )
            }
            _ => return Err(ResponseError::SomethingWentWrong),
        };

        Ok(Response::Single(response))
    }
}
