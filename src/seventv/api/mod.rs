pub(super) mod schema;

use reqwest::Client;

use self::schema::{ConnectionUser, EmoteSet, User};

pub struct SevenTVAPIClient {
    client: Client,
}

const SEVENTV_URL: &str = "https://7tv.io/v3";

impl SevenTVAPIClient {
    pub fn new(client: Client) -> Self {
        Self { client }
    }

    pub async fn get_user_by_twitch_id(&self, user_id: String) -> Option<ConnectionUser> {
        let url = format!("{SEVENTV_URL}/users/twitch/{user_id}");
        let request = self.client.get(url).send().await;

        if let Ok(response) = request {
            if let Ok(data) = response.json::<ConnectionUser>().await {
                return Some(data);
            }
        }

        None
    }

    pub async fn get_user(&self, id: String) -> Option<User> {
        let url = format!("{SEVENTV_URL}/users/{id}");
        let request = self.client.get(url).send().await;

        if let Ok(response) = request {
            if let Ok(data) = response.json::<User>().await {
                return Some(data);
            }
        }

        None
    }

    pub async fn get_emote_set(&self, id: String) -> Option<EmoteSet> {
        let url = format!("{SEVENTV_URL}/emote-sets/{id}");
        let request = self.client.get(url).send().await;

        if let Ok(response) = request {
            if let Ok(data) = response.json::<EmoteSet>().await {
                return Some(data);
            }
        }

        None
    }
}
