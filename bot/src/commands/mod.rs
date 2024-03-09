use crate::{
    instance_bundle::InstanceBundle,
    localization::LineId,
    modules::{
        custom_command::CustomCommandsCommand, ecount::EmoteCountCommand,
        esim::EmoteSimilarityCommand, etop::EmoteTopCommand, event::EventCommand,
        holiday::HolidayCommand, join::JoinCommand, massping::MasspingCommand,
        mcsrv::MinecraftServerCommand, notify::NotifyCommand, ping::PingCommand,
        settings::SettingsCommand, spam::SpamCommand, timer::TimerCommand, userid::UserIdCommand,
    },
    shared_variables::{
        DEFAULT_COMMAND_DELAY_SEC, DEFAULT_COMMAND_LEVEL_OF_RIGHTS, DEFAULT_COMMAND_OPTIONS,
        DEFAULT_COMMAND_SUBCOMMANDS,
    },
};
use async_trait::async_trait;
use common::models::LevelOfRights;
use eyre::Result;

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
    fn required_rights(&self) -> LevelOfRights {
        DEFAULT_COMMAND_LEVEL_OF_RIGHTS
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
                Box::new(SettingsCommand),
                Box::new(EmoteCountCommand),
                Box::new(EmoteTopCommand),
                Box::new(EmoteSimilarityCommand),
                Box::new(UserIdCommand),
                Box::new(MinecraftServerCommand),
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

#[derive(Debug)]
pub enum CommandArgument {
    Subcommand,
    Message,
    Interval,
    Name,
    Target,
    Value,
    Amount,
}

impl CommandArgument {
    pub fn to_line_id(&self) -> LineId {
        match self {
            Self::Subcommand => LineId::ArgumentSubcommand,
            Self::Message => LineId::ArgumentMessage,
            Self::Interval => LineId::ArgumentInterval,
            Self::Name => LineId::ArgumentName,
            Self::Target => LineId::ArgumentTarget,
            Self::Value => LineId::ArgumentValue,
            Self::Amount => LineId::ArgumentAmount,
        }
    }
}
