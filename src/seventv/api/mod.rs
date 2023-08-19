use reqwest::Client;

use self::schemes::ConnectionUser;

pub mod schemes;

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
}
