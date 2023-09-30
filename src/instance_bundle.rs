use std::sync::Arc;

use twitch_irc::{login::StaticLoginCredentials, SecureTCPTransport, TwitchIRCClient};

use crate::localization::Localizator;

pub struct InstanceBundle {
    pub twitch_irc_client: Arc<TwitchIRCClient<SecureTCPTransport, StaticLoginCredentials>>,
    pub localizator: Arc<Localizator>,
}
