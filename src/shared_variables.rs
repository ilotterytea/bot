use lazy_static::lazy_static;
use std::time::Instant;

lazy_static! {
    /// The time when the program was started.
    pub static ref START_TIME: Instant = Instant::now();
}
