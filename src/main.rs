use std::sync::Arc;
use tokio::sync::Mutex;
use twitch_irc::login::StaticLoginCredentials;
use twitch_irc::TwitchIRCClient;
use twitch_irc::{ClientConfig, SecureTCPTransport};
use crate::api::command::CommandLoader;
use crate::shared_variables::CONFIGURATION;

mod api;
mod config;
mod locale;
mod shared_variables;

#[tokio::main]
pub async fn main() {
    // default configuration is to join chat as anonymous.
    let (mut incoming_messages, client) =
        TwitchIRCClient::<SecureTCPTransport, StaticLoginCredentials>::new(
            ClientConfig::new_simple(StaticLoginCredentials::new(
                CONFIGURATION.twitch.bot_name.clone(),
                Some(CONFIGURATION.twitch.oauth2_token.clone()),
            )),
        );

    let command_loader = Arc::new(Mutex::from(CommandLoader::new()));

    // join a channel
    // This function only returns an error if the passed channel login name is malformed,
    // so in this simple case where the channel name is hardcoded we can ignore the potential
    // error with `unwrap`.
    client
        .join(CONFIGURATION.twitch.bot_name.to_owned())
        .unwrap();

    // keep the tokio executor alive.
    // If you return instead of waiting the background task will exit.
    join_handle.await.unwrap();
}