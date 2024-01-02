use async_trait::async_trait;
use eyre::Result;
use twitch_api::helix::chat::GetChattersRequest;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    models::diesel::LevelOfRights,
};

pub struct MasspingCommand;

#[async_trait]
impl Command for MasspingCommand {
    fn get_name(&self) -> String {
        "massping".to_string()
    }

    fn required_rights(&self) -> LevelOfRights {
        LevelOfRights::Moderator
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let twitch_request = GetChattersRequest::new(
            request.channel.alias_id.to_string(),
            instance_bundle.twitch_api_token.user_id.clone(),
        );

        let chatters = match instance_bundle
            .twitch_api_client
            .req_get(twitch_request, &*instance_bundle.twitch_api_token.clone())
            .await
        {
            Ok(response) => response.data,
            Err(e) => {
                return Err(ResponseError::InsufficientRights);
            }
        };

        let message = request.message.unwrap_or_default();
        let mut lines: Vec<String> = vec!["".to_string()];
        let mut index = 0;

        for chatter in chatters {
            if format!(
                "{}@{}, {}",
                lines.get(index).unwrap(),
                chatter.user_login,
                message
            )
            .len()
                >= 500
            {
                index += 1;
            }

            let line = match lines.get(index) {
                Some(line) => {
                    let line = line.into();
                    lines.remove(index);
                    line
                }
                None => "".to_string(),
            };

            lines.insert(index, format!("{}@{}, ", line, chatter.user_login));
        }

        Ok(Response::Multiple(
            lines
                .iter()
                .map(|x| format!("{}{}", x, message))
                .collect::<Vec<String>>(),
        ))
    }
}
