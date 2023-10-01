use substring::Substring;

pub struct ParsedPrivmsgMessage {
    pub command_id: String,
    pub message: String,
}

impl ParsedPrivmsgMessage {
    pub fn parse(message: &str, prefix: char) -> Option<Self> {
        let mut message_split = message.split(" ").collect::<Vec<&str>>();
        let message = String::new();

        let command_id = if let Some(word) = message_split.first() {
            if word.starts_with(prefix) {
                word.substring(1, word.len()).to_string()
            } else {
                return None;
            }
        } else {
            return None;
        };

        message_split.remove(0);

        Some(Self {
            command_id,
            message: message_split.join(" "),
        })
    }
}
