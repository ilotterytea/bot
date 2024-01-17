use serde::Deserialize;

pub const BOT_CONFIGURATION_FILE: &str = include_str!("../../Bot.toml");

#[derive(Deserialize, Default, Debug, Clone)]
pub struct Configuration {
    pub credentials: Credentials,
    pub database_connection: DatabaseConnection,
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
    pub redirect_uri: String,
}

#[derive(Deserialize, Default, Debug, Clone)]
pub struct DatabaseConnection {
    pub username: String,
    pub password: String,
    pub hostname: String,
    pub database_name: String,
}
