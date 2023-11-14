use async_trait::async_trait;
use twitch_api::helix::chat::GetChattersRequest;
use twitch_irc::message::PrivmsgMessage;

use crate::{
    commands::Command,
    instance_bundle::InstanceBundle,
    localization::LineId,
    message::ParsedPrivmsgMessage,
    models::diesel::{Channel, ChannelPreference, User},
};

pub struct MasspingCommand;

#[async_trait]
impl Command for MasspingCommand {
    fn get_name(&self) -> String {
        "massping".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        data_message: PrivmsgMessage,
        message: ParsedPrivmsgMessage,
        _channel: &Channel,
        channel_preferences: &ChannelPreference,
        _user: &User,
    ) -> Option<Vec<String>> {
        let request = GetChattersRequest::new(
            data_message.channel_id.clone(),
            instance_bundle.twitch_api_token.user_id.clone(),
        );

        let chatters = match instance_bundle
            .twitch_api_client
            .req_get(request, &*instance_bundle.twitch_api_token.clone())
            .await
        {
            Ok(response) => response.data,
            Err(e) => {
                return Some(vec![instance_bundle
                    .localizator
                    .get_formatted_text(
                        channel_preferences.language.clone().unwrap().as_str(),
                        LineId::MsgError,
                        vec![data_message.sender.name, e.to_string()],
                    )
                    .unwrap()])
            }
        };

        let message = message.message.unwrap();
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

        Some(
            lines
                .iter()
                .map(|x| format!("{}{}", x, message))
                .collect::<Vec<String>>(),
        )
    }
}
