use std::sync::Arc;

use async_trait::async_trait;
use eyre::Result;
use twitch_api::{twitch_oauth2::UserToken, types::NicknameRef, HelixClient};

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command, CommandArgument,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    seventv::api::{
        schema::{Emote, EmoteSet},
        SevenTVAPIClient,
    },
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
        if let Some(message) = request.message.clone() {
            let message_split = message.split_ascii_whitespace().collect::<Vec<&str>>();

            let (origin_channel, target_channel) = if message_split.len() > 1 {
                (
                    message_split.first().unwrap().to_string(),
                    message_split.get(1).unwrap().to_string(),
                )
            } else {
                (
                    request.channel.alias_name.clone(),
                    message_split.first().unwrap().to_string(),
                )
            };

            if let Some(origin_emote_set) = self
                .get_emote_set(
                    origin_channel.clone(),
                    instance_bundle.twitch_api_client.clone(),
                    instance_bundle.twitch_api_token.clone(),
                    instance_bundle.seventv_api_client.clone(),
                )
                .await
            {
                if let Some(target_emote_set) = self
                    .get_emote_set(
                        target_channel.clone(),
                        instance_bundle.twitch_api_client.clone(),
                        instance_bundle.twitch_api_token.clone(),
                        instance_bundle.seventv_api_client.clone(),
                    )
                    .await
                {
                    let final_emote_set = origin_emote_set
                        .emotes
                        .iter()
                        .filter(|x| target_emote_set.emotes.iter().any(|y| x.id.eq(&y.id)))
                        .collect::<Vec<&Emote>>();

                    let percentage = ((final_emote_set.len() as f32
                        / origin_emote_set.emotes.len() as f32)
                        * 100.0)
                        .trunc();

                    if final_emote_set.is_empty() {
                        return Ok(Response::Single(
                            instance_bundle.localizator.formatted_text_by_request(
                                &request,
                                LineId::EmoteSimilaritySetNotSimilar,
                                vec![
                                    instance_bundle
                                        .localizator
                                        .get_literal_text(
                                            request.channel_preference.language.as_str(),
                                            LineId::Provider7TV,
                                        )
                                        .unwrap(),
                                    origin_channel,
                                    target_channel,
                                ],
                            ),
                        ));
                    }

                    return Ok(Response::Single(
                        instance_bundle.localizator.formatted_text_by_request(
                            &request,
                            LineId::EmoteSimilaritySetSimilar,
                            vec![
                                instance_bundle
                                    .localizator
                                    .get_literal_text(
                                        request.channel_preference.language.as_str(),
                                        LineId::Provider7TV,
                                    )
                                    .unwrap(),
                                origin_channel,
                                target_channel,
                                final_emote_set.len().to_string(),
                                origin_emote_set.emotes.len().to_string(),
                                percentage.to_string(),
                            ],
                        ),
                    ));
                } else {
                    return Err(ResponseError::NotFound(target_channel));
                }
            } else {
                return Err(ResponseError::NotFound(origin_channel));
            }
        }

        Err(ResponseError::NotEnoughArguments(CommandArgument::Target))
    }
}

impl EmoteSimilarityCommand {
    async fn get_emote_set(
        &self,
        twitch_user_name: String,
        twitch_api: Arc<HelixClient<'static, reqwest::Client>>,
        twitch_token: Arc<UserToken>,
        seventv_api: Arc<SevenTVAPIClient>,
    ) -> Option<EmoteSet> {
        if let Ok(Some(user)) = twitch_api
            .get_user_from_login(
                NicknameRef::from_str(twitch_user_name.as_str()),
                &*twitch_token,
            )
            .await
        {
            if let Some(stv_user) = seventv_api.get_user_by_twitch_id(user.id.take()).await {
                return Some(stv_user.emote_set);
            }
        }

        None
    }
}
