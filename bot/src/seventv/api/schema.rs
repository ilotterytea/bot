use serde::Deserialize;

#[derive(Deserialize, Debug)]
pub struct ConnectionUser {
    pub id: String,
    pub platform: String,
    pub username: String,
    pub emote_set: EmoteSet,
    pub user: User,
}

#[derive(Deserialize, Debug)]
pub struct Connection {
    pub id: String,
    pub platform: String,
    pub username: String,
}

#[derive(Deserialize, Debug)]
pub struct User {
    pub id: String,
    pub username: String,
    pub connections: Vec<Connection>,
}

#[derive(Deserialize, Debug)]
pub struct EmoteSet {
    pub id: String,
    pub name: String,
    pub emotes: Vec<Emote>,
    pub owner: Option<EmoteSetOwner>,
}

#[derive(Deserialize, Debug)]
pub struct EmoteSetOwner {
    pub id: String,
    pub username: String,
}

#[derive(Deserialize, Debug)]
pub struct Emote {
    pub id: String,
    pub name: String,
    pub actor_id: Option<String>,
}
