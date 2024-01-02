use substring::Substring;

use crate::commands::CommandLoader;

#[derive(Clone)]
pub struct ParsedPrivmsgMessage {
    pub command_id: String,
    pub message: Option<String>,
}

impl ParsedPrivmsgMessage {
    pub fn parse(message: &str, prefix: char, command_loader: &CommandLoader) -> Option<Self> {
        let mut message_split = message.split(" ").collect::<Vec<&str>>();
        let message = String::new();

        let command_id = if let Some(word) = message_split.first() {
            if word.starts_with(prefix) {
                let word = word.substring(1, word.len()).to_string();

                if command_loader
                    .commands
                    .iter()
                    .find(|x| x.get_name().eq(&word))
                    .is_some()
                {
                    word
                } else {
                    return None;
                }
            } else {
                return None;
            }
        } else {
            return None;
        };

        message_split.remove(0);
        let msg = message_split.join(" ");

        Some(Self {
            command_id,
            message: if message_split.is_empty() {
                None
            } else {
                Some(msg)
            },
        })
    }
}
