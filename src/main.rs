use std::sync::Arc;
use tokio::sync::Mutex;
use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::TwitchIRCClient;
use twitch_irc::{ClientConfig, SecureTCPTransport};
use crate::api::command::CommandLoader;

mod api;

#[tokio::main]
pub async fn main() {
    // default configuration is to join chat as anonymous.
    let config = ClientConfig::default();
    let (mut incoming_messages, client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(config);

    let command_loader = Arc::new(Mutex::from(CommandLoader::new()));

    // join a channel
    // This function only returns an error if the passed channel login name is malformed,
    // so in this simple case where the channel name is hardcoded we can ignore the potential
    // error with `unwrap`.
    client.join("imteabot".to_owned()).unwrap();

    // keep the tokio executor alive.
    // If you return instead of waiting the background task will exit.
    join_handle.await.unwrap();
}