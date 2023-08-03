use crate::api::message::ParsedMessage;
use crate::commands;
use crate::models::diesel::{Channel, User};
use async_trait::async_trait;

use super::InstanceBundle;

#[async_trait]
/// The default trait for commands.
pub trait Command {
    /// Get the name ID of the command.
    fn get_name_id(&self) -> String;

    /// Run the command.
    async fn run(
        &self,
        instance_bundle: &InstanceBundle,
        channel: Channel,
        user: User,
        message: ParsedMessage,
    ) -> Option<Vec<String>>;
}

/// Command loader.
pub struct CommandLoader {
    pub commands: Vec<Box<dyn Command + Send + Sync>>,
}

impl CommandLoader {
    /// Create a new instance of command loader.
    pub fn new() -> Self {
        Self {
            commands: vec![Box::new(commands::ping::PingCommand)],
        }
    }

    /// Run the command.
    /// Returns None if command has no response.
    pub async fn run(
        &self,
        instance_bundle: &InstanceBundle,
        channel: Channel,
        user: User,
        message: ParsedMessage,
    ) -> Option<Vec<String>> {
        let wrapped_command = &self
            .commands
            .iter()
            .find(|it| it.get_name_id().eq(&message.command_id));

        if wrapped_command.is_none() {
            return None;
        }

        wrapped_command
            .unwrap()
            .run(instance_bundle, channel, user, message)
            .await
    }
}
