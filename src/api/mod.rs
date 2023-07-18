use reqwest::Client;
use twitch_api::{twitch_oauth2::UserToken, HelixClient};
use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

pub mod command;
pub mod message;

/// Instance bundle.
pub struct InstanceBundle<'a> {
    /// Twitch IRC client.
    pub twitch_client: &'a TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>,
    /// Twitch API client.
    pub twitch_api_client: &'a HelixClient<'static, Client>,
    /// A token for Twitch API client.
    pub twitch_api_token: &'a UserToken,
}
