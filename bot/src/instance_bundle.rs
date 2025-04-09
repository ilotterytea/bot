use std::{collections::HashSet, sync::Arc};

use common::config::Configuration;
use reqwest::Client;
use tokio::sync::Mutex;
use twitch_api::{HelixClient, twitch_oauth2::UserToken, types::UserId};
use twitch_emotes::{
    betterttv::BetterTTVWSClient,
    seventv::{SevenTVAPIClient, SevenTVWSClient},
};
use twitch_irc::{SecureTCPTransport, TwitchIRCClient, login::StaticLoginCredentials};

use crate::localization::Localizator;

pub struct InstanceBundle {
    pub twitch_irc_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    pub twitch_api_client: Arc<HelixClient<'static, Client>>,
    pub twitch_api_token: Arc<UserToken>,
    pub localizator: Arc<Localizator>,
    pub configuration: Arc<Configuration>,

    pub twitch_livestream_websocket_data: Arc<Mutex<HashSet<UserId>>>,
    pub stv_client: Arc<Mutex<SevenTVWSClient>>,
    pub stv_api_client: Arc<SevenTVAPIClient>,
    pub bttv_client: Arc<Mutex<BetterTTVWSClient>>,
}
