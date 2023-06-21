use crate::api::command::CommandLoader;
use crate::api::message::ParsedMessage;
use tokio::sync::MutexGuard;
use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::PrivmsgMessage;
use twitch_irc::{SecureTCPTransport, TwitchIRCClient};

/// The handler for Twitch IRC messages.
pub async fn irc_message_handler(
    client: &TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>,
    command_loader: MutexGuard<'_, CommandLoader>,
    message: PrivmsgMessage,
) {
    println!("Received message: {:?}", message);

    let wrapped_parsed_msg = ParsedMessage::parse(&command_loader, &message.message_text);

    if wrapped_parsed_msg.is_some() {
        let parsed_msg = wrapped_parsed_msg.unwrap();

        let command = command_loader.run(&message, parsed_msg).await;

        if command.is_some() {
            for line in command.unwrap() {
                client
                    .say(message.channel_login.to_owned(), line)
                    .await
                    .expect("Couldn't send the message!")
            }
        }
    }
}