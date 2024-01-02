use twitch_api::types::UserId;

pub mod livestream;

pub struct WebsocketData {
    pub awaiting_channel_ids: Vec<UserId>,
    pub listening_channel_ids: Vec<UserId>,
}

impl Default for WebsocketData {
    fn default() -> Self {
        Self {
            awaiting_channel_ids: Vec::new(),
            listening_channel_ids: Vec::new(),
        }
    }
}
