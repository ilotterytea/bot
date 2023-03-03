use twitch_irc::{
    login::StaticLoginCredentials, message::PrivmsgMessage, SecureTCPTransport, TwitchIRCClient,
};

use crate::{establish_connection, managers::command_loader::CommandLoader};

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

    let response = loader.run(
        &mut establish_connection(),
        &message.message_text.as_str(),
        &message.sender.id.as_str(),
        &message.channel_id.as_str(),
    );

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
