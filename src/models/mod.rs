pub mod diesel;
pub mod stats;

use serde::Deserialize;

#[derive(Deserialize)]
pub struct IvrUserResponse {
    pub banned: bool,
    #[serde(rename = "banReason")]
    pub ban_reason: Option<String>,
    pub login: String,
    pub id: String,
}
