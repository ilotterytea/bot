use std::env;

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

pub fn get_configuration() -> Result<Configuration, toml::de::Error> {
    let toml: Configuration = toml::from_str(BOT_CONFIGURATION_FILE)?;

    // for establish_connection() method
    if env::var("DATABASE_URL").is_err() {
        env::set_var(
            "DATABASE_URL",
            format!(
                "postgres://{}:{}@{}/{}",
                toml.database_connection.username,
                toml.database_connection.password,
                toml.database_connection.hostname,
                toml.database_connection.database_name
            ),
        );
    }

    Ok(toml)
}
