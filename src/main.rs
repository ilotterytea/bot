use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::message::ServerMessage;
use twitch_irc::TwitchIRCClient;
use twitch_irc::{ClientConfig, SecureTCPTransport};

mod handlers;

#[tokio::main]
async fn main() {
    let config = ClientConfig::default();

    let (mut incoming_messages, client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(config);

    client.join("ilotterytea".to_owned()).unwrap();

    let join_handle = tokio::spawn(async move {
        while let Some(message) = incoming_messages.recv().await {
            println!("Received message: {:?}", message);

            match message {
                ServerMessage::Privmsg(msg) => {
                    handlers::irc_message_handler(&client, msg);
                }
                _ => {}
            }
        }
    });

    join_handle.await.unwrap();
}
