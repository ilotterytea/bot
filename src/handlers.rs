use crate::api::command::CommandLoader;
use crate::api::message::ParsedMessage;
use crate::models::diesel::{Channel, NewChannel, NewUser, User};
use crate::schema::{channels::dsl as ch, users::dsl as us};
use crate::utils::establish_connection;
use diesel::{insert_into, ExpressionMethods, QueryDsl, RunQueryDsl};
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
    let conn = &mut establish_connection();

    // Getting the channel or creating if somehow it doesn't exist
    let mut channel = ch::channels
        .filter(ch::alias_id.eq(message.channel_id.parse::<i32>().unwrap()))
        .first::<Channel>(conn)
        .unwrap_or_else(|_| {
            insert_into(ch::channels)
                .values(vec![NewChannel {
                    alias_id: message.channel_id.parse::<i32>().unwrap(),
                    alias_name: message.channel_login.as_str(),
                }])
                .execute(conn)
                .expect("Failed to create a new channel");

            let c = ch::channels
                .filter(ch::alias_id.eq(message.channel_id.parse::<i32>().unwrap()))
                .first::<Channel>(conn)
                .expect("Failed to get a channel after creating it");

            c
        });

    // Update the channel data
    channel.alias_name = message.channel_login.clone();

    // Getting the sender or creating if user is new for the bot
    let mut user = us::users
        .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
        .first::<User>(conn)
        .unwrap_or_else(|_| {
            insert_into(us::users)
                .values(vec![NewUser {
                    alias_id: message.sender.id.parse::<i32>().unwrap(),
                    alias_name: message.sender.login.as_str(),
                }])
                .execute(conn)
                .expect("Failed to create a new user");

            let u = us::users
                .filter(us::alias_id.eq(message.sender.id.parse::<i32>().unwrap()))
                .first::<User>(conn)
                .expect("Failed to get a user even after creating it");

            u
        });

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
