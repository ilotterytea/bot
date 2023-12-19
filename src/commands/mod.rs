use crate::{
    instance_bundle::InstanceBundle,
    message::ParsedPrivmsgMessage,
    models::diesel::{Channel, ChannelPreference, User},
    modules::{
        custom_command::CustomCommandsCommand, event::EventCommand, holiday::HolidayCommand,
        join::JoinCommand, massping::MasspingCommand, notify::NotifyCommand, ping::PingCommand,
        spam::SpamCommand, timer::TimerCommand,
    },
    shared_variables::{
        DEFAULT_COMMAND_DELAY_SEC, DEFAULT_COMMAND_OPTIONS, DEFAULT_COMMAND_SUBCOMMANDS,
    },
};
use async_trait::async_trait;
use eyre::Result;
use twitch_irc::message::PrivmsgMessage;

use self::{
    request::Request,
    response::{Response, ResponseError},
};

pub mod request;
pub mod response;

#[async_trait]
pub trait Command {
    fn get_name(&self) -> String;
    fn get_delay_sec(&self) -> i32 {
        DEFAULT_COMMAND_DELAY_SEC
    }
    fn get_options(&self) -> Vec<String> {
        DEFAULT_COMMAND_OPTIONS
    }
    fn get_subcommands(&self) -> Vec<String> {
        DEFAULT_COMMAND_SUBCOMMANDS
    }
    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError>;
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
                Box::new(TimerCommand),
                Box::new(CustomCommandsCommand),
                Box::new(EventCommand),
                Box::new(NotifyCommand),
            ],
        }
    }

    pub async fn execute_command(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        if let Some(command) = self
            .commands
            .iter()
            .find(|x| x.get_name().eq(request.command_id.as_str()))
        {
            return command.execute(instance_bundle, request).await;
        }
        Err(ResponseError::SomethingWentWrong)
    }
}
