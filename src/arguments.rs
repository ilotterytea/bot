use twitch_irc::{
    login::StaticLoginCredentials, message::PrivmsgMessage, SecureTCPTransport, TwitchIRCClient,
};

use crate::managers::command_loader::CommandLoader;

pub struct Arguments {
    pub message: PrivmsgMessage,
    pub client: &'static TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>,
    pub loader: &'static CommandLoader,
}
