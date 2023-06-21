use std::fs::read_to_string;
use serde::Deserialize;

/// Bot configuration.
#[derive(Deserialize)]
pub struct Configuration {
    pub default_prefix: Option<String>,
    pub twitch: Twitch,
}

/// The Twitch bot configuration section.
#[derive(Deserialize)]
pub struct Twitch {
    pub bot_name: String,
    pub oauth2_token: String,
}

impl Configuration {
    /// Load and parse the configuration file as TOML.
    /// Panic if file does not exist, insufficient permission, or other I/O error.
    pub fn load(file_name: &str) -> Self {
        let contents = read_to_string(file_name)
            .unwrap_or_else(|e| panic!("Failed to open {}: {}", file_name, e));

        toml::from_str(contents.as_str()).unwrap()
    }
}
