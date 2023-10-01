use twitch_irc::message::PrivmsgMessage;

use crate::{
    command::CommandLoader, instance_bundle::InstanceBundle, message::ParsedPrivmsgMessage,
};

pub async fn handle_chat_message(
    instance_bundle: InstanceBundle,
    command_loader: &CommandLoader,
    message: PrivmsgMessage,
) {
    let parsed_message = ParsedPrivmsgMessage::parse(message.message_text.as_str(), "~");

    if let Some(parsed_message) = parsed_message {
        if let Ok(Some(response)) = command_loader
            .execute_command(&instance_bundle, parsed_message)
            .await
        {
            for line in response {
                instance_bundle
                    .twitch_irc_client
                    .say(message.channel_login.clone(), line)
                    .await
                    .expect("Failed to send message");
            }
        };
    }
}
