use std::time::Instant;

use lazy_static::lazy_static;

use common::models::LevelOfRights;

lazy_static! {
    pub static ref START_TIME: Instant = Instant::now();
}

pub const DEFAULT_COMMAND_DELAY_SEC: i32 = 5;
pub const DEFAULT_COMMAND_OPTIONS: Vec<String> = Vec::new();
pub const DEFAULT_COMMAND_SUBCOMMANDS: Vec<String> = Vec::new();
pub const DEFAULT_COMMAND_LEVEL_OF_RIGHTS: LevelOfRights = LevelOfRights::User;

#[cfg(debug_assertions)]
pub const DEFAULT_PREFIX: &str = "~";

#[cfg(not(debug_assertions))]
pub const DEFAULT_PREFIX: &str = "!";

pub const DEFAULT_LANGUAGE: &str = "english";

pub const HOLIDAY_V1_API_URL: &str = "https://hol.ilotterytea.kz/api/v1";
pub const SEVENTV_WEBSOCKET_URL: &str = "wss://events.7tv.io/v3";
pub const IVR_API_V2_URL: &str = "https://api.ivr.fi/v2";
pub const MCSRV_API_URL: &str = "https://api.mcsrvstat.us/3";
pub const PASTE_API_URL: &str = "https://paste.ilotterytea.kz";

pub const TIMER_CHECK_DELAY: u64 = 1;

pub const COMPILE_TIMESTAMP: i32 = compile_time::unix!();
pub const COMPILE_VERSION: &str = compile_time::rustc_version_str!();
