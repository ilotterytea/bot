use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize)]
#[serde(default)]
pub struct DatabaseConfiguration {
    pub url: String,
}

#[derive(Deserialize, Serialize)]
#[serde(default)]
pub struct WebConfiguration {
    pub port: u16,
    pub contact_name: String,
    pub contact_url: String,
    pub bot_title: String,
}

#[derive(Deserialize, Serialize)]
#[serde(default)]
pub struct BotConfiguration {
    pub username: String,
    pub password: String,
    pub client_id: Option<String>,
    pub client_secret: Option<String>,
    pub redirect_uri: Option<String>,
    pub owner_twitch_id: Option<u32>,
}

#[derive(Deserialize, Serialize)]
#[serde(default)]
pub struct CommandsConfiguration {
    pub default_prefix: String,
    pub default_language: String,
    pub spam: CommandsSpamConfiguration,
}

#[derive(Deserialize, Serialize)]
#[serde(default)]
pub struct CommandsSpamConfiguration {
    pub max_count: u32,
}

#[derive(Deserialize, Serialize)]
#[serde(default)]
pub struct ThirdPartyConfiguration {
    pub docs_url: String,
    pub stats_api_url: Option<String>,
    pub stats_api_password: Option<String>,
    pub pastea_api_url: String,
    pub pastea_api_password: Option<String>,
}

#[derive(Deserialize, Serialize, Default)]
#[serde(default)]
pub struct Configuration {
    pub database: DatabaseConfiguration,
    pub web: WebConfiguration,
    pub bot: BotConfiguration,
    pub commands: CommandsConfiguration,
    pub third_party: ThirdPartyConfiguration,
}

impl Configuration {
    pub fn load() -> Self {
        let path = "rustpilled_bot.toml";
        let Ok(contents) = std::fs::read_to_string(path) else {
            return Self::default();
        };

        toml::from_str(&contents).expect("Error loading rustpilled_bot.toml configuration")
    }
}

impl Default for DatabaseConfiguration {
    fn default() -> Self {
        Self {
            url: "database.db".into(),
        }
    }
}

impl Default for WebConfiguration {
    fn default() -> Self {
        Self {
            port: 8080,
            contact_name: "someone".into(),
            contact_url: "#".into(),
            bot_title: "ilotterytea's twitch bot".into(),
        }
    }
}

impl Default for BotConfiguration {
    fn default() -> Self {
        Self {
            username: "".into(),
            password: "".into(),
            client_id: None,
            client_secret: None,
            redirect_uri: None,
            owner_twitch_id: None,
        }
    }
}

impl Default for CommandsConfiguration {
    fn default() -> Self {
        Self {
            default_prefix: "!".into(),
            default_language: "english".into(),
            spam: CommandsSpamConfiguration::default(),
        }
    }
}

impl Default for CommandsSpamConfiguration {
    fn default() -> Self {
        Self { max_count: 50 }
    }
}

impl Default for ThirdPartyConfiguration {
    fn default() -> Self {
        Self {
            docs_url: "https://bot.ilotterytea.kz/wiki".into(),
            stats_api_url: None,
            stats_api_password: None,
            pastea_api_url: "https://paste.ilotterytea.kz".into(),
            pastea_api_password: None,
        }
    }
}
