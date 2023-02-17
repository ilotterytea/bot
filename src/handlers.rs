use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::PrivmsgMessage;
use twitch_irc::SecureTCPTransport;
use twitch_irc::TwitchIRCClient;

use crate::managers::command_loader::CommandLoader;

pub async fn irc_message_handler(
    _client: &TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>,
    message: PrivmsgMessage,
    cmdloader: &CommandLoader,
) {
    if message.message_text == "test" {
        _client
            .say(message.channel_login.to_owned(), "test !!!".to_owned())
            .await
            .expect("Unable to send a message to chat!");
    }

    let response = cmdloader.run(&message.message_text);

    if !&response.is_none() {
        _client
            .say(
                message.channel_login.to_owned(),
                response.unwrap().to_owned(),
            )
            .await
            .expect("Unable to send a message to chat!");
    }
}
