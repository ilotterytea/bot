use twitch_irc::message::PrivmsgMessage;

use crate::{
    commands::{request::Request, response::Response, CommandLoader},
    instance_bundle::InstanceBundle,
    message::ParsedPrivmsgMessage,
    utils::diesel::{create_action, establish_connection},
};

pub async fn handle_chat_message(
    instance_bundle: InstanceBundle,
    command_loader: &CommandLoader,
    message: PrivmsgMessage,
) {
    let conn = &mut establish_connection();

    if let Some(request) = Request::try_from(&message, "~", command_loader, conn) {
        let response = command_loader
            .execute_command(&instance_bundle, request.clone())
            .await;

        // TODO: CREATE ACTION LOG LATER

        match response {
            Ok(r) => match r {
                Response::Single(line) => {
                    instance_bundle
                        .twitch_irc_client
                        .say(message.channel_login.clone(), line)
                        .await
                        .expect("Failed to send message");
                }
                Response::Multiple(lines) => {
                    for line in lines {
                        instance_bundle
                            .twitch_irc_client
                            .say(message.channel_login.clone(), line)
                            .await
                            .expect("Failed to send message");
                    }
                }
            },
            Err(_) => {}
        }
    }
}
