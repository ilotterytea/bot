use serde::{Deserialize, Serialize};

use super::api::schema::{Emote, User};

#[derive(Serialize, Deserialize, Debug)]
pub struct Payload<T> {
    pub op: u32,
    pub d: T,
}

#[derive(Deserialize, Debug)]
pub struct Dispatch {
    #[serde(rename(deserialize = "type"))]
    pub event_type: String,
    pub body: DispatchBody,
}

#[derive(Deserialize, Debug)]
pub struct DispatchBody {
    pub id: String,
    pub actor: User,
    pub added: Option<Vec<ChangeField>>,
    pub updated: Option<Vec<ChangeField>>,
    pub removed: Option<Vec<ChangeField>>,
    pub pushed: Option<Vec<ChangeField>>,
    pub pulled: Option<Vec<ChangeField>>,
}

#[derive(Deserialize, Debug)]
pub struct ChangeField {
    pub key: String,
    pub old_value: Option<Emote>,
    pub value: Option<Emote>,
}

#[derive(Deserialize, Debug)]
pub struct Hello {
    pub subscription_limit: u32,
    pub session_id: String,
}

#[derive(Deserialize, Debug)]
pub struct Ack {
    pub command: String,
    pub data: String,
}

#[derive(Serialize, Debug)]
pub struct Subscribe {
    #[serde(rename(serialize = "type"))]
    pub event_type: String,
    pub condition: SubscribeCondition,
}

#[derive(Serialize, Debug)]
pub struct SubscribeCondition {
    pub object_id: String,
}

#[derive(Serialize)]
pub struct Resume {
    pub session_id: String,
}
