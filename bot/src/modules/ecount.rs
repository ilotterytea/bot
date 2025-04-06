use async_trait::async_trait;
use eyre::Result;
use reqwest::{Client, StatusCode};

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command, CommandArgument,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
};

pub struct EmoteCountCommand;

#[async_trait]
impl Command for EmoteCountCommand {
    fn get_name(&self) -> String {
        "ecount".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let Some(hostname) = &instance_bundle.configuration.third_party.stats_api_url else {
            return Err(ResponseError::SomethingWentWrong);
        };

        let Some(emote_name) = &request.message else {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Value));
        };

        let client = Client::new();

        let response = client
            .get(format!(
                "{}/api/emotes/channel/{}",
                hostname, request.channel.alias_id
            ))
            .send()
            .await
            .expect("Error sending HTTP request");

        if response.status() != StatusCode::OK {
            return Err(ResponseError::NotFound(request.channel.alias_name));
        }

        let json: serde_json::Value = response
            .json()
            .await
            .expect("Error serializing HTTP response");

        let data: &Vec<serde_json::Value> = json.get("data").unwrap().as_array().unwrap();

        for emote in data {
            let code = emote.get("code").unwrap().as_str().unwrap();

            if code.ne(emote_name) {
                continue;
            }

            let usage = emote.get("usage").unwrap().as_i64().unwrap();
            let provider_name = emote.get("provider_name").unwrap().as_str().unwrap();
            let provider_name = provider_name.to_uppercase();

            return Ok(Response::Single(
                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::EmoteCountUsage,
                    vec![provider_name, code.to_string(), usage.to_string()],
                ),
            ));
        }

        Err(ResponseError::NotFound(emote_name.clone()))
    }
}
