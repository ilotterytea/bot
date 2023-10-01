use crate::{
    commands::ping::PingCommand, instance_bundle::InstanceBundle, message::ParsedPrivmsgMessage,
};
use async_trait::async_trait;
use twitch_irc::message::PrivmsgMessage;

#[async_trait]
pub trait Command {
    fn get_name(&self) -> String;
    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        data_message: PrivmsgMessage,
        message: ParsedPrivmsgMessage,
    ) -> Option<Vec<String>>;
}

pub struct CommandLoader {
    pub commands: Vec<Box<dyn Command + Send + Sync>>,
}

impl CommandLoader {
    pub fn new() -> Self {
        Self {
            commands: vec![Box::new(PingCommand)],
        }
    }

    pub async fn execute_command(
        &self,
        instance_bundle: &InstanceBundle,
        data_message: PrivmsgMessage,
        message: ParsedPrivmsgMessage,
    ) -> Result<Option<Vec<String>>, &str> {
        if let Some(command) = self
            .commands
            .iter()
            .find(|x| x.get_name().eq(message.command_id.as_str()))
        {
            return Ok(command
                .execute(instance_bundle, data_message, message)
                .await);
        }
        Err("bruh")
    }
}
