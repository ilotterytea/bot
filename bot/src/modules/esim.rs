use async_trait::async_trait;
use twitch_api::{helix::users::GetUsersRequest, types::Nickname};

use crate::{
    commands::{
        Command, CommandArgument,
        request::Request,
        response::{Response, ResponseError},
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
};

pub struct EmoteSimilarityCommand;

#[async_trait]
impl Command for EmoteSimilarityCommand {
    fn get_name(&self) -> String {
        "esim".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let Some(message) = &request.message else {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Target));
        };

        let parts = message.split(' ').collect::<Vec<&str>>();

        let (origin_name, target_name) = match (parts.get(0), parts.get(1)) {
            (Some(o), Some(t)) => (o.to_string(), t.to_string()),
            (Some(o), _) => (request.channel.alias_name.clone(), o.to_string()),
            _ => return Err(ResponseError::NotEnoughArguments(CommandArgument::Target)),
        };

        let logins = [
            Nickname::new(origin_name.clone()),
            Nickname::new(target_name.clone()),
        ];
        let http_request = GetUsersRequest::logins(&logins);

        let Ok(users) = instance_bundle
            .twitch_api_client
            .req_get(http_request, &*instance_bundle.twitch_api_token)
            .await
        else {
            return Err(ResponseError::NotFound(format!(
                "{} (Twitch API fail)",
                message
            )));
        };

        let users = users.data;

        let (Some(origin_user), Some(target_user)) = (
            users.iter().find(|x| x.login.as_str().eq(&origin_name)),
            users.iter().find(|x| x.login.as_str().eq(&target_name)),
        ) else {
            return Err(ResponseError::NotFound(format!(
                "{} (Twitch user fail)",
                message
            )));
        };

        let (Some(origin_stv_user), Some(target_stv_user)) = (
            instance_bundle
                .stv_api_client
                .get_user_by_twitch_id(origin_user.id.as_str().parse::<usize>().unwrap())
                .await,
            instance_bundle
                .stv_api_client
                .get_user_by_twitch_id(target_user.id.as_str().parse::<usize>().unwrap())
                .await,
        ) else {
            return Err(ResponseError::NotFound(format!(
                "{} (7TV User fail)",
                message
            )));
        };

        let (Some(origin_emote_set), Some(target_emote_set)) = (
            instance_bundle
                .stv_api_client
                .get_emote_set(&origin_stv_user.emote_set_id)
                .await,
            instance_bundle
                .stv_api_client
                .get_emote_set(&target_stv_user.emote_set_id)
                .await,
        ) else {
            return Err(ResponseError::NotFound(format!(
                "{} (EmoteSet fail)",
                message
            )));
        };

        let max_emotes = target_emote_set.emotes.len();
        let mut matched_emotes = 0;

        for emote in target_emote_set.emotes {
            if origin_emote_set.emotes.iter().any(|x| x.id.eq(&emote.id)) {
                matched_emotes += 1;
            }
        }

        let percentage = (matched_emotes as f32 / max_emotes as f32) * 100.0;

        Ok(Response::Single(
            instance_bundle.localizator.formatted_text_by_request(
                &request,
                if matched_emotes == 0 {
                    LineId::EmoteSimilaritySetNotSimilar
                } else {
                    LineId::EmoteSimilaritySetSimilar
                },
                if matched_emotes == 0 {
                    vec!["(7TV)".to_string(), origin_name, target_name]
                } else {
                    vec![
                        "(7TV)".to_string(),
                        origin_name,
                        target_name,
                        matched_emotes.to_string(),
                        max_emotes.to_string(),
                        format!("{:.1}", percentage),
                    ]
                },
            ),
        ))
    }
}
