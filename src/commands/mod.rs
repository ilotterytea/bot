use crate::{
    instance_bundle::InstanceBundle,
    message::ParsedPrivmsgMessage,
    models::diesel::{Channel, ChannelPreference, User},
    modules::{
        holiday::HolidayCommand, join::JoinCommand, massping::MasspingCommand, ping::PingCommand,
        spam::SpamCommand,
    },
    shared_variables::{DEFAULT_COMMAND_DELAY_SEC, DEFAULT_COMMAND_OPTIONS},
};
use async_trait::async_trait;
use twitch_irc::message::PrivmsgMessage;

#[async_trait]
pub trait Command {
    fn get_name(&self) -> String;
    fn get_delay_sec(&self) -> i32 {
        DEFAULT_COMMAND_DELAY_SEC
    }
    fn get_options(&self) -> Vec<String> {
        DEFAULT_COMMAND_OPTIONS
    }
    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        data_message: PrivmsgMessage,
        message: ParsedPrivmsgMessage,
        channel: &Channel,
        channel_preferences: &ChannelPreference,
        user: &User,
    ) -> Option<Vec<String>>;
}

pub struct CommandLoader {
    pub commands: Vec<Box<dyn Command + Send + Sync>>,
}

impl CommandLoader {
    pub fn new() -> Self {
        Self {
            commands: vec![
                Box::new(PingCommand),
                Box::new(SpamCommand),
                Box::new(MasspingCommand),
                Box::new(HolidayCommand),
                Box::new(JoinCommand),
            ],
        }
    }

    pub async fn execute_command(
        &self,
        instance_bundle: &InstanceBundle,
        data_message: PrivmsgMessage,
        message: ParsedPrivmsgMessage,
        channel: &Channel,
        channel_preferences: &ChannelPreference,
        user: &User,
    ) -> Result<Option<Vec<String>>, &str> {
        if let Some(command) = self
            .commands
            .iter()
            .find(|x| x.get_name().eq(message.command_id.as_str()))
        {
            return Ok(command
                .execute(
                    instance_bundle,
                    data_message,
                    message,
                    channel,
                    channel_preferences,
                    user,
                )
                .await);
        }
        Err("bruh")
    }
}
