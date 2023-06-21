use std::time::Instant;
use lazy_static::lazy_static;

lazy_static! {
    /// The time when the program was started.
    pub static ref START_TIME: Instant = Instant::now();
}