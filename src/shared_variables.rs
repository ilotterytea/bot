use std::time::Instant;

use lazy_static::lazy_static;

lazy_static! {
    pub static ref START_TIME: Instant = Instant::now();
}

pub const DEFAULT_COMMAND_DELAY_SEC: i32 = 5;

pub const DEFAULT_PREFIX: char = '~';
pub const DEFAULT_LANGUAGE: &str = "english";

pub const HOLIDAY_V1_API_URL: &str = "https://hol.ilotterytea.kz/api/v1";
