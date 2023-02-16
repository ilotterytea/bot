use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::PrivmsgMessage;
use twitch_irc::SecureTCPTransport;
use twitch_irc::TwitchIRCClient;

pub async fn irc_message_handler(
    _client: &TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>,
    message: PrivmsgMessage,
) {
    if message.message_text == "test" {
        _client
            .say(message.channel_login.to_owned(), "test !!!".to_owned())
            .await
            .expect("Unable to send a message to chat!");
    }
    println!("{}", message.message_text);
}
