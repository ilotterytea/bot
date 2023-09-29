use crate::command::CommandLoader;
use twitch_irc::{
    login::StaticLoginCredentials, message::ServerMessage, ClientConfig, SecureTCPTransport,
    TwitchIRCClient,
};

mod command;

#[tokio::main]
async fn main() {
    println!("Hello, world!");

    let command_loader = CommandLoader::new();
    let (mut irc_incoming_messages, irc_client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(ClientConfig::default());

    irc_client.join("ilotterytea".into()).unwrap();

    let irc_thread = tokio::spawn(async move {
        while let Some(irc_message) = irc_incoming_messages.recv().await {
            match irc_message {
                ServerMessage::Privmsg(message) => {
                    println!("received message: {:?}", message);
                }
                _ => {
                    println!("not handled message: {:?}", irc_message);
                }
            }
        }
    });

    tokio::join!(irc_thread);
}
