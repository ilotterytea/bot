use serde::Deserialize;
use std::collections::HashMap;

#[derive(Deserialize)]
pub struct ServerData {
    pub ip: String,
    pub hostname: Option<String>,
    pub motd: Option<HashMap<String, Vec<String>>>,
    pub players: Option<ServerPlayerData>,
    pub protocol: Option<ServerProtocol>,
    pub online: bool,
}

#[derive(Deserialize)]
pub struct ServerProtocol {
    pub version: u32,
    pub name: Option<String>,
}

#[derive(Deserialize)]
pub struct ServerPlayerData {
    pub online: i32,
    pub max: i32,
}
