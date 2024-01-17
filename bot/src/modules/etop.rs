use async_trait::async_trait;
use eyre::Result;
use twitch_api::types::NicknameRef;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    models::stats::{ChannelEmote, ChannelEmoteUsage, Response as StatsResponse},
    shared_variables::STATS_API_V1_URL,
};

pub struct EmoteTopCommand;

#[async_trait]
impl Command for EmoteTopCommand {
    fn get_name(&self) -> String {
        "etop".to_string()
    }

    fn get_subcommands(&self) -> Vec<String> {
        vec!["desc".to_string(), "asc".to_string()]
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let subcommand_id = match request.subcommand_id.clone() {
            Some(v) => v,
            None => "desc".to_string(),
        };

        let message = request.message.clone().unwrap_or_default();
        let message_split = message.split_ascii_whitespace().collect::<Vec<&str>>();

        let mut amount: usize = 10;

        let origin_name = match message_split.first() {
            Some(v) => {
                let v = if let Ok(x) = v.to_string().parse::<usize>() {
                    amount = x;
                    request.channel.alias_name.clone()
                } else {
                    v.to_string()
                };

                v
            }
            None => request.channel.alias_name.clone(),
        };

        match message_split.get(1) {
            Some(v) => {
                if let Ok(x) = v.to_string().parse::<usize>() {
                    amount = x;
                }
            }
            None => {}
        }

        if let Ok(Some(user)) = instance_bundle
            .twitch_api_client
            .get_user_from_login(
                NicknameRef::from_str(origin_name.as_str()),
                &*instance_bundle.twitch_api_token,
            )
            .await
        {
            let channel_id = user.id.take();

            if let Some(response) = self.fetch_channel_emotes(channel_id.clone()).await {
                if response.status_code != 200 {
                    return Err(ResponseError::ExternalAPIError(
                        response.status_code,
                        response.message,
                    ));
                }

                let emotes = response.data.unwrap_or_default();

                if let Some(response) = self.fetch_channel_emote_usages(channel_id).await {
                    if response.status_code != 200 {
                        return Err(ResponseError::ExternalAPIError(
                            response.status_code,
                            response.message,
                        ));
                    }

                    let emote_usages = response.data.unwrap_or_default();

                    let mut usages: Vec<(String, i32)> = Vec::new();

                    for usage in emote_usages {
                        if let Some(emote) = emotes.iter().find(|x| x.emote_id.eq(&usage.emote_id))
                        {
                            if let Some(u_emote) = usages.iter_mut().find(|x| x.0.eq(&emote.name)) {
                                if u_emote.1 < usage.usage_count {
                                    u_emote.1 = usage.usage_count;
                                }

                                continue;
                            }

                            usages.push((emote.name.clone(), usage.usage_count));
                        }
                    }

                    usages.sort_by(|a, b| {
                        if subcommand_id.eq("asc") {
                            a.1.cmp(&b.1)
                        } else {
                            b.1.cmp(&a.1)
                        }
                    });

                    if amount > 50 {
                        amount = 50;
                    }

                    if amount > usages.len() {
                        amount = usages.len()
                    }

                    usages.drain(amount..);

                    let mut message_parts: Vec<String> = Vec::new();

                    for usage in usages {
                        message_parts.push(format!("{} ({})", usage.0, usage.1));
                    }

                    if message_parts.is_empty() {
                        return Ok(Response::Single(
                            instance_bundle.localizator.formatted_text_by_request(
                                &request,
                                LineId::EmoteTopNoEmotes,
                                vec![
                                    instance_bundle
                                        .localizator
                                        .get_literal_text(
                                            request.channel_preference.language.as_str(),
                                            LineId::Provider7TV,
                                        )
                                        .unwrap(),
                                    origin_name,
                                ],
                            ),
                        ));
                    }

                    return Ok(Response::Single(
                        instance_bundle.localizator.formatted_text_by_request(
                            &request,
                            LineId::EmoteTopResponse,
                            vec![
                                instance_bundle
                                    .localizator
                                    .get_literal_text(
                                        request.channel_preference.language.as_str(),
                                        LineId::Provider7TV,
                                    )
                                    .unwrap(),
                                origin_name,
                                amount.to_string(),
                                instance_bundle
                                    .localizator
                                    .get_literal_text(
                                        request.channel_preference.language.as_str(),
                                        if subcommand_id.eq("asc") {
                                            LineId::MiscAscending
                                        } else {
                                            LineId::MiscDescending
                                        },
                                    )
                                    .unwrap(),
                                message_parts.join(", "),
                            ],
                        ),
                    ));
                }
            }
        } else {
            return Err(ResponseError::NotFound(origin_name));
        }

        Err(ResponseError::SomethingWentWrong)
    }
}

impl EmoteTopCommand {
    async fn fetch_channel_emotes(
        &self,
        channel_id: String,
    ) -> Option<StatsResponse<Vec<ChannelEmote>>> {
        let url = format!(
            "{}/api/v1/channel/twitch/{}/emotes",
            STATS_API_V1_URL, channel_id
        );

        if let Ok(response) = reqwest::get(url).await {
            if let Ok(data) = response.json::<StatsResponse<Vec<ChannelEmote>>>().await {
                return Some(data);
            }
        }

        None
    }
    async fn fetch_channel_emote_usages(
        &self,
        channel_id: String,
    ) -> Option<StatsResponse<Vec<ChannelEmoteUsage>>> {
        let url = format!(
            "{}/api/v1/channel/twitch/{}/emotes/usage",
            STATS_API_V1_URL, channel_id
        );

        if let Ok(response) = reqwest::get(url).await {
            if let Ok(data) = response
                .json::<StatsResponse<Vec<ChannelEmoteUsage>>>()
                .await
            {
                return Some(data);
            }
        }

        None
    }
}
