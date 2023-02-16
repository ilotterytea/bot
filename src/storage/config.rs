use serde::Deserialize;

#[derive(Deserialize)]
pub struct Config {
    pub credentials: Credentials,
}

#[derive(Deserialize)]
pub struct Credentials {
    pub oauth_token: Option<String>,
    pub bot_name: Option<String>,
    pub access_token: Option<String>,
    pub client_id: Option<String>,
    pub client_secret: Option<String>,
}
