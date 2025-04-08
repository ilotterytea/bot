use async_trait::async_trait;

use crate::{
    commands::{
        Command, CommandArgument,
        request::Request,
        response::{Response, ResponseError},
    },
    instance_bundle::InstanceBundle,
};

use common::models::LevelOfRights;

pub struct SpamCommand;

#[async_trait]
impl Command for SpamCommand {
    fn get_name(&self) -> String {
        "spam".to_string()
    }

    fn required_rights(&self) -> LevelOfRights {
        LevelOfRights::Moderator
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        if let Some(msg) = request.message {
            let mut s = msg.split(' ').collect::<Vec<&str>>();
            let max_count = instance_bundle.configuration.commands.spam.max_count;

            let count = if let Some(c) = s.first() {
                if let Ok(c) = c.parse::<u32>() {
                    s.remove(0);

                    if c > max_count { max_count } else { c }
                } else {
                    10
                }
            } else {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Amount));
            };

            let msg = s.join(" ");

            if msg.is_empty() {
                return Err(ResponseError::NotEnoughArguments(CommandArgument::Message));
            }

            let mut msgs = Vec::<String>::new();

            for _ in 0..count {
                msgs.push(msg.clone());
            }

            return Ok(Response::Multiple(msgs));
        }

        Err(ResponseError::NotEnoughArguments(CommandArgument::Message))
    }
}
