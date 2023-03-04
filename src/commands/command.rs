use crate::arguments::Arguments;

use super::{permissions::Permissions, MessageCommandArguments};

pub struct CommandData {
    pub id: String,
    pub delay: usize,
    pub permissions: Permissions,
    pub options: Vec<String>,
    pub subcommands: Vec<String>,
    pub aliases: Vec<String>,
    pub run: fn(cmd_args: &MessageCommandArguments, data_args: &Arguments) -> Option<String>,
}

pub trait CommandBehavior {
    fn new() -> Self;
    fn run(cmd_args: &MessageCommandArguments, data_args: &Arguments) -> Option<String>;
}
