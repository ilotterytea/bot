use serde::Deserialize;

pub const BOT_CONFIGURATION_FILE: &str = include_str!("../../Bot.toml");

#[derive(Deserialize, Default, Debug, Clone)]
pub struct Configuration {
    pub credentials: Credentials,
}

#[derive(Deserialize, Default, Debug, Clone)]
pub struct Credentials {
    pub username: String,
    pub password: String,
    pub twitch_app: Option<TwitchCredentials>,
}

#[derive(Deserialize, Default, Debug, Clone)]
pub struct TwitchCredentials {
    pub client_id: String,
    pub client_secret: String,
}
