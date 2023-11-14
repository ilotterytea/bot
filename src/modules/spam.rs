use async_trait::async_trait;
use twitch_irc::message::PrivmsgMessage;

use crate::{
    commands::Command,
    instance_bundle::InstanceBundle,
    localization::LineId,
    message::ParsedPrivmsgMessage,
    models::diesel::{Channel, ChannelPreference, User},
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
        data_message: PrivmsgMessage,
        message: ParsedPrivmsgMessage,
        _channel: &Channel,
        channel_preferences: &ChannelPreference,
        _user: &User,
    ) -> Option<Vec<String>> {
        let msg = message.message.unwrap();
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
                    channel_preferences.language.clone().unwrap().as_str(),
                    LineId::CommandSpamNoCount,
                    vec![data_message.sender.name],
                )
                .unwrap()]);
        };

        if count <= 0 {
            return Some(vec![instance_bundle
                .localizator
                .get_formatted_text(
                    channel_preferences.language.clone().unwrap().as_str(),
                    LineId::CommandSpamInvalidCount,
                    vec![data_message.sender.name, s.first().unwrap().to_string()],
                )
                .unwrap()]);
        }

        s.remove(0);

        let msg = s.join(" ");

        if msg.is_empty() {
            return Some(vec![instance_bundle
                .localizator
                .get_formatted_text(
                    channel_preferences.language.clone().unwrap().as_str(),
                    LineId::MsgNoMessage,
                    vec![data_message.sender.name],
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
