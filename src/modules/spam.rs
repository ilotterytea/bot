use async_trait::async_trait;

use crate::{
    commands::{request::Request, Command},
    instance_bundle::InstanceBundle,
    localization::LineId,
};

pub struct SpamCommand;

#[async_trait]
impl Command for SpamCommand {
    fn get_name(&self) -> String {
        "spam".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Option<Vec<String>> {
        let msg = request.message.unwrap();
        let mut s = msg.split(' ').collect::<Vec<&str>>();

        let count = if let Some(c) = s.first() {
            if let Ok(c) = c.parse::<i32>() {
                c
            } else {
                -1
            }
        } else {
            return Some(vec![instance_bundle
                .localizator
                .get_formatted_text(
                    request.channel_preference.language.as_str(),
                    LineId::CommandSpamNoCount,
                    vec![request.sender.alias_name],
                )
                .unwrap()]);
        };

        if count <= 0 {
            return Some(vec![instance_bundle
                .localizator
                .get_formatted_text(
                    request.channel_preference.language.as_str(),
                    LineId::CommandSpamInvalidCount,
                    vec![request.sender.alias_name, s.first().unwrap().to_string()],
                )
                .unwrap()]);
        }

        s.remove(0);

        let msg = s.join(" ");

        if msg.is_empty() {
            return Some(vec![instance_bundle
                .localizator
                .get_formatted_text(
                    request.channel_preference.language.as_str(),
                    LineId::MsgNoMessage,
                    vec![request.sender.alias_name],
                )
                .unwrap()]);
        }

        let mut msgs = Vec::<String>::new();

        for _ in 0..count {
            msgs.push(msg.clone());
        }

        Some(msgs)
    }
}
