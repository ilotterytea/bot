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
    shared_variables::PASTE_API_URL,
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

        let paste_request = reqwest::Client::new()
            .post(format!("{}/paste", PASTE_API_URL))
            .multipart(multipart)
            .send()
            .await
            .expect("Failed to send a request to paste service");

        println!("{}", paste_request.status());

        if paste_request.status() != StatusCode::CREATED {
            return Err(ResponseError::ExternalAPIError(
                paste_request.status().as_u16() as u32,
                None,
            ));
        }

        let response: Value = paste_request.json::<Value>().await.unwrap();

        Ok(Response::Single(
            instance_bundle.localizator.formatted_text_by_request(
                &request,
                LineId::ChattersResponse,
                vec![format!(
                    "{}/{}",
                    PASTE_API_URL,
                    response["data"]["id"].as_str().unwrap()
                )],
            ),
        ))
    }
}
