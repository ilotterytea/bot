use std::time::Instant;

use lazy_static::lazy_static;

lazy_static! {
    pub static ref START_TIME: Instant = Instant::now();
}

pub const DEFAULT_PREFIX: char = '~';
