use crate::arguments::Arguments;
use crate::commands::command::{CommandBehavior, CommandData};
use crate::commands::permissions::Permissions;
use crate::commands::MessageCommandArguments;

pub struct Ping(pub CommandData);

impl CommandBehavior for Ping {
    fn new() -> Self {
        Ping(CommandData {
            id: String::from("ping"),
            delay: 5,
            options: vec![],
            subcommands: vec![],
            aliases: vec!["пинг".to_string(), "pong".to_string()],
            permissions: Permissions::USER,
            run: Self::run,
        })
    }
    fn run(cmd_args: &MessageCommandArguments, data_args: &Arguments) -> Option<String> {
        Some(String::from("Pong!"))
    }
}
