use async_trait::async_trait;
use reqwest::{Client, StatusCode};

use crate::{
    commands::{
        Command,
        request::Request,
        response::{Response, ResponseError},
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
};

pub struct EmoteTopCommand;

#[async_trait]
impl Command for EmoteTopCommand {
    fn get_name(&self) -> String {
        "etop".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let hostname = &instance_bundle.configuration.third_party.stats_api_url;

        // --- PARSING USER INPUT ---
        let providers = ["ttv", "bttv", "ffz", "7tv", "all"];

        let (provider_name, target): (String, String);

        if let Some(message) = &request.message {
            let parts = message.split(' ').collect::<Vec<&str>>();

            if parts.len() > 1 {
                let first_word = parts[0];
                let second_word = parts[1];

                if providers.contains(&first_word) {
                    provider_name = first_word.to_string();
                } else {
                    provider_name = "all".into();
                }

                if second_word.eq("me") {
                    target = "me".into();
                } else {
                    target = "channel".into();
                }
            } else {
                let first_word = parts[0];

                if providers.contains(&first_word) {
                    provider_name = first_word.to_string();
                } else {
                    provider_name = "all".into();
                }

                if first_word.eq("me") {
                    target = "me".into();
                } else {
                    target = "channel".into();
                }
            }
        } else {
            provider_name = "all".into();
            target = "channel".into();
        }

        let provider_url_name = if provider_name.eq("all") {
            "".into()
        } else {
            format!("/{}", provider_name)
        };

        let (target_url_name, target_name) = if target.eq("channel") {
            ("".into(), request.channel.alias_name.clone())
        } else {
            (
                format!("/{}", request.sender.alias_id),
                request.sender.alias_name.clone(),
            )
        };

        // --- SENDING REQUEST ---
        let client = Client::new();

        let response = client
            .get(format!(
                "{}/api/emotes{}/channel/{}{}",
                hostname, provider_url_name, request.channel.alias_id, target_url_name
            ))
            .send()
            .await
            .expect("Error sending HTTP request");

        if response.status() != StatusCode::OK {
            return Err(ResponseError::NotFound(target_name));
        }

        // --- PARSING RESPONSE ---
        let json: serde_json::Value = response
            .json()
            .await
            .expect("Error serializing HTTP response");

        let data: &Vec<serde_json::Value> = json.get("data").unwrap().as_array().unwrap();

        let mut emote_count = 0;
        let mut emote_usages: Vec<String> = Vec::new();

        for emote in data {
            if emote_count >= 5 {
                break;
            }

            let code = emote.get("code").unwrap().as_str().unwrap();
            let usage = emote.get("usage").unwrap().as_i64().unwrap();

            emote_usages.push(format!("{} ({})", code, usage));
            emote_count += 1;
        }

        return Ok(Response::Single(
            instance_bundle.localizator.formatted_text_by_request(
                &request,
                LineId::EmoteTopResponse,
                vec![
                    target_name,
                    emote_count.to_string(),
                    provider_name.to_uppercase(),
                    emote_usages.join(", "),
                ],
            ),
        ));
    }
}
