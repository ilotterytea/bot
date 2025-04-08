use async_trait::async_trait;
use chrono::Utc;
use eyre::Result;
use reqwest::StatusCode;
use serde_json::Value;
use twitch_api::helix::chat::GetChattersRequest;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
};

pub struct ChattersCommand;

#[async_trait]
impl Command for ChattersCommand {
    fn get_name(&self) -> String {
        "chatters".to_string()
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
            Err(_) => {
                return Err(ResponseError::InsufficientRights);
            }
        };

        let mut body = String::new();

        body.push_str(&format!(
            "total chatters: {}\n-------------------\n\n",
            chatters.len()
        ));

        for c in chatters {
            body.push_str(c.user_login.take().as_str());
            body.push('\n');
        }

        let time = Utc::now();
        let timestamp = time.format("%d.%m.%Y %H:%M:%S");

        let title = format!(
            "{}'s chatter list on {}",
            request.channel.alias_name, timestamp
        );

        let multipart = reqwest::multipart::Form::new()
            .text("paste", body)
            .text("title", title);

        let mut paste_request = reqwest::Client::new()
            .post(format!(
                "{}/api/paste/upload",
                &instance_bundle.configuration.third_party.pastea_api_url
            ))
            .multipart(multipart);

        if let Some(password) = &instance_bundle
            .configuration
            .third_party
            .pastea_api_password
        {
            paste_request =
                paste_request.header("Authorization", format!("Rustpastes {}", password));
        }

        let response = paste_request
            .send()
            .await
            .expect("Failed to send a request to paste service");

        if response.status() != StatusCode::CREATED {
            return Err(ResponseError::ExternalAPIError(
                response.status().as_u16() as u32,
                None,
            ));
        }

        let response: Value = response.json::<Value>().await.unwrap();

        Ok(Response::Single(
            instance_bundle.localizator.formatted_text_by_request(
                &request,
                LineId::ChattersResponse,
                vec![format!(
                    "{}/{}",
                    &instance_bundle.configuration.third_party.pastea_api_url,
                    response["data"]["id"].as_str().unwrap()
                )],
            ),
        ))
    }
}
