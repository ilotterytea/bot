use std::{collections::HashSet, sync::Arc};

use reqwest::Client;
use tokio::sync::Mutex;
use twitch_api::{twitch_oauth2::UserToken, types::UserId, HelixClient};
use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

use crate::{localization::Localizator, seventv::api::SevenTVAPIClient};

pub struct InstanceBundle {
    pub twitch_irc_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    pub twitch_api_client: Arc<HelixClient<'static, Client>>,
    pub twitch_api_token: Arc<UserToken>,
    pub localizator: Arc<Localizator>,

    pub twitch_livestream_websocket_data: Arc<Mutex<HashSet<UserId>>>,

    pub seventv_api_client: Arc<SevenTVAPIClient>,
    pub seventv_eventapi_data: Arc<Mutex<HashSet<UserId>>>,
}
