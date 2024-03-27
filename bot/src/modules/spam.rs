use std::env;

use async_trait::async_trait;
use eyre::Result;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command, CommandArgument,
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
        _instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        if let Some(msg) = request.message {
            let mut s = msg.split(' ').collect::<Vec<&str>>();
            let max_count = env::var("MODULE_SPAM_MAXCOUNT").unwrap_or_else(|_| "100".to_string());
            let max_count = max_count.parse::<u32>().unwrap();

            let count = if let Some(c) = s.first() {
                if let Ok(c) = c.parse::<u32>() {
                    s.remove(0);

                    if c > max_count {
                        max_count
                    } else {
                        c
                    }
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
