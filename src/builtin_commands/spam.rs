use crate::arguments::Arguments;
use crate::commands::command::{CommandBehavior, CommandData};
use crate::commands::permissions::Permissions;
use crate::commands::MessageCommandArguments;

pub struct Spam(pub CommandData);

impl CommandBehavior for Spam {
    fn new() -> Self {
        Spam(CommandData {
            id: String::from("spam"),
            delay: 1,
            options: vec!["count".to_string()],
            subcommands: vec![],
            aliases: vec!["спам".to_string()],
            permissions: Permissions::MOD,
            run: Self::run,
        })
    }
    fn run(cmd_args: &MessageCommandArguments, data_args: &Arguments) -> Option<Vec<String>> {
        if cmd_args.message.is_none() {
            return Some(vec![
                "No message for spam or number of required messages!".to_string()
            ]);
        }

        let binding = cmd_args.message.clone().unwrap();

        let mut s = binding.split(" ").collect::<Vec<&str>>();
        let _count = s.get(0).unwrap().parse::<i32>();

        if _count.is_err() {
            return Some(vec![format!(
                "The amount of messages is not a number! ({})",
                s.get(0).unwrap()
            )]);
        }

        let mut c = _count.unwrap();
        let max_count = 64;

        if c > max_count {
            c = max_count;
        }

        s.remove(0);

        if s.len() == 0 {
            return Some(vec!["No message.".to_string()]);
        }

        let m = s.join(" ");
        let mut msgs: Vec<String> = Vec::new();

        for i in 0..c {
            msgs.push(format!(
                "{} {}",
                m.clone(),
                if cmd_args.options.contains(&"count".to_string()) {
                    (i + 1).to_string()
                } else {
                    "".to_string()
                }
            ));
        }

        Some(msgs)
    }
}
