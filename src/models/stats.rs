use serde::Deserialize;

#[derive(Deserialize)]
pub struct Response<T> {
    pub status_code: u32,
    pub message: Option<String>,
    pub data: Option<T>,
}

#[derive(Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct ChannelEmote {
    pub emote_id: i32,
    pub name: String,
}

#[derive(Deserialize, PartialEq, Eq, PartialOrd, Ord)]
pub struct ChannelEmoteUsage {
    pub emote_id: i32,
    pub usage_count: i32,
}
