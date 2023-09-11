use reqwest::Client;
use std::sync::Arc;
use tokio::sync::Mutex;
use twitch_api::{twitch_oauth2::UserToken, HelixClient};
use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

use crate::{
    livestream::EventsubLivestreamData,
    seventv::{api::SevenTVAPIClient, websocket::SevenTVWebsocketClient},
};

pub mod command;
pub mod message;

/// Instance bundle.
pub struct InstanceBundle {
    /// Twitch IRC client.
    pub twitch_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    /// Twitch API client.
    pub twitch_api_client: Arc<HelixClient<'static, Client>>,
    /// A token for Twitch API client.
    pub twitch_api_token: Arc<UserToken>,
    // Twitch EventSub client.
    pub twitch_eventsub_data: Arc<Mutex<EventsubLivestreamData>>,
    // 7TV EventAPI client.
    pub seventv_eventapi_client: Arc<Mutex<SevenTVWebsocketClient>>,
    // 7TV API client.
    pub seventv_api_client: Arc<SevenTVAPIClient>,
}
