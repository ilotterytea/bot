use std::sync::Arc;

use reqwest::Client;
use twitch_api::{twitch_oauth2::UserToken, HelixClient};
use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

use crate::localization::Localizator;

pub struct InstanceBundle {
    pub twitch_irc_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    pub twitch_api_client: Arc<HelixClient<'static, Client>>,
    pub twitch_api_token: Arc<UserToken>,
    pub localizator: Arc<Localizator>,
}
