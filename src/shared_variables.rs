use std::time::Instant;
use lazy_static::lazy_static;
use crate::config::Configuration;

lazy_static! {
    /// The time when the program was started.
    pub static ref START_TIME: Instant = Instant::now();

    /// Bot configuration.
    pub static ref CONFIGURATION: Configuration = Configuration::load("config.toml");
}