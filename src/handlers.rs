use twitch_irc::{
    login::StaticLoginCredentials, message::PrivmsgMessage, SecureTCPTransport, TwitchIRCClient,
};

use crate::{
    arguments::Arguments, commands::MessageCommandArguments, establish_connection,
    managers::command_loader::CommandLoader,
};

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

    let args = Arguments::generate(
        &mut establish_connection(),
        message.sender.id.clone(),
        message.channel_id.clone(),
    );

    let prefix = args.preferences.prefix.clone().unwrap_or("!".to_string());

    if message.message_text.starts_with(prefix.as_str()) {
        let cmd_args =
            MessageCommandArguments::parse(loader, &message.message_text.as_str(), prefix.as_str());

        if cmd_args.is_some() {
            let response = loader.run(
                &mut establish_connection(),
                cmd_args.unwrap(),
                args,
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
    }
}
