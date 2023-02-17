use crate::commands::command::{CommandBehavior, CommandData};

use crate::builtin_commands::ping::Ping;

pub struct CommandLoader {
    commands: Vec<CommandData>,
}

impl CommandLoader {
    pub fn new() -> Self {
        let mut commands: Vec<CommandData> = vec![];

        commands.push(Ping::new().0);

        CommandLoader { commands }
    }
    pub fn run(&self, id: &String) -> Option<String> {
        let mut response: Option<String> = None;

        for cid in &self.commands {
            if (&cid).id.eq(id) {
                response = ((&cid).run)();
            }
        }

        response
    }
}
