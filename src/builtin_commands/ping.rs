use crate::commands::command::{CommandBehavior, CommandData};
use crate::commands::permissions::Permissions;

pub struct Ping(pub CommandData);

impl CommandBehavior for Ping {
    fn new() -> Self {
        Ping(CommandData {
            id: String::from("ping"),
            delay: 1000,
            options: vec![],
            subcommands: vec![],
            permissions: Permissions::USER,
            run: Self::run,
        })
    }
    fn run() -> Option<String> {
        Some(String::from("Pong!"))
    }
}
