use twitch_irc::{
    login::StaticLoginCredentials, message::PrivmsgMessage, SecureTCPTransport, TwitchIRCClient,
};

use crate::managers::command_loader::CommandLoader;

pub async fn irc_message_handler(
    client: &TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>,
    loader: &CommandLoader,
    message: PrivmsgMessage,
) {
    if message.message_text == "test" {
        client
            .say(message.channel_login.to_owned(), "test !!!".to_owned())
            .await
            .expect("Unable to send a message to chat!");
    }

    let response = loader.run(&message.message_text);

    if !&response.is_none() {
        client
            .say(
                message.channel_login.to_owned(),
                response.unwrap().to_owned(),
            )
            .await
            .expect("Unable to send a message to chat!");
    }
}
