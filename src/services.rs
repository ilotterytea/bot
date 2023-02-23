use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

use crate::managers::command_loader::CommandLoader;

pub struct Services {
    pub client: &'static TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>,
    pub loader: &'static CommandLoader,
}
