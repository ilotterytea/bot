use crate::api::command::CommandLoader;

/// Parsed message.
pub struct ParsedMessage {
    /// Command ID.
    pub command_id: String,
    /// Final message without command ID and etc.
    pub message: Option<String>
}

impl ParsedMessage {
    /// Parses the message into a new ParsedMessage.
    /// Returns None if the message does not contain an existing command in the bot.
    pub fn parse(command_loader: &CommandLoader, message: &str) -> Option<Self> {
        let mut split_message: Vec<&str> = message.trim().split(' ').collect();

        if split_message.is_empty() {
            return None;
        }

        let command_id = split_message.first().unwrap().to_owned();
        let _command = command_loader.commands
            .iter()
            .find(|it| it.get_name_id().eq(command_id))?;

        split_message.remove(0);

        Some(Self {
            command_id: command_id.to_string(),
            message: if split_message.is_empty() {
                None
            } else {
                Some(split_message.join(" "))
            }
        })
    }
}