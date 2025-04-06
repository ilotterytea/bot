use std::sync::Arc;

use crate::{
    instance_bundle::InstanceBundle,
    localization::LineId,
    modules::{
        chatters::ChattersCommand, custom_command::CustomCommandsCommand,
        ecount::EmoteCountCommand, esim::EmoteSimilarityCommand, etop::EmoteTopCommand,
        event::EventCommand, help::HelpCommand, holiday::HolidayCommand, join::JoinCommand,
        massping::MasspingCommand, mcsrv::MinecraftServerCommand, notify::NotifyCommand,
        ping::PingCommand, settings::SettingsCommand, spam::SpamCommand, timer::TimerCommand,
        userid::UserIdCommand,
    },
    shared_variables::{
        DEFAULT_COMMAND_DELAY_SEC, DEFAULT_COMMAND_LEVEL_OF_RIGHTS, DEFAULT_COMMAND_OPTIONS,
        DEFAULT_COMMAND_SUBCOMMANDS,
    },
};
use async_trait::async_trait;
use common::models::LevelOfRights;
use eyre::Result;
use include_dir::Dir;
use mlua::{Function, Lua, Table, Value};

use self::{
    request::Request,
    response::{Response, ResponseError},
};

pub mod request;
pub mod response;

const MODULE_DIRECTORY: Dir<'_> = include_dir::include_dir!("./modules");

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

#[derive(Debug)]
pub struct LuaCommand {
    pub name: String,
    pub delay_sec: u32,
    pub options: Vec<String>,
    pub subcommands: Vec<String>,
    pub minimal_rights: LevelOfRights,
    pub handle: Function,
}

pub struct CommandLoader {
    pub rust_commands: Vec<Box<dyn Command + Send + Sync>>,
    pub lua_commands: Vec<LuaCommand>,
    pub lua: Lua,
    pub instance_bundle: Arc<InstanceBundle>,
}

impl CommandLoader {
    pub fn new(instance_bundle: Arc<InstanceBundle>) -> Self {
        Self {
            rust_commands: vec![
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
                Box::new(HelpCommand),
                Box::new(ChattersCommand),
            ],
            lua_commands: Vec::new(),
            lua: Lua::new(),
            instance_bundle,
        }
    }

    pub async fn load(&mut self) -> mlua::Result<()> {
        log::info!("Loading Lua API...");
        self.load_api()?;

        log::info!("Loading Lua commands...");
        self.load_directory(&MODULE_DIRECTORY)?;

        log::info!("Finished!");
        Ok(())
    }

    pub async fn execute_command(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        if let Some(command) = self
            .rust_commands
            .iter()
            .find(|x| x.get_name().eq(request.command_id.as_str()))
        {
            return command.execute(instance_bundle, request).await;
        }

        let Some(command) = self
            .lua_commands
            .iter()
            .find(|x| x.name.eq(&request.command_id))
        else {
            return Err(ResponseError::NotRegisteredCommand(
                request.command_id.clone(),
            ));
        };

        self.lua
            .sandbox(true)
            .expect("Failed to enable sandbox mode");

        match command.handle.call_async::<Value>(()).await {
            Ok(v) => match v {
                Value::String(v) => Ok(Response::Single(v.to_string_lossy())),
                Value::Table(t) => Ok(Response::Multiple(
                    t.sequence_values::<String>()
                        .collect::<mlua::Result<_>>()
                        .expect("Error collecting table"),
                )),
                _ => Err(ResponseError::LuaUnsupportedResponseType(
                    v.type_name().to_string(),
                )),
            },
            Err(e) => Err(ResponseError::LuaExecutionError(e)),
        }
    }

    fn load_api(&self) -> mlua::Result<()> {
        Ok(())
    }

    fn load_directory(&mut self, dir: &Dir<'_>) -> mlua::Result<()> {
        for entry in dir.entries() {
            if let Some(dir) = entry.as_dir() {
                self.load_directory(dir)?;
                continue;
            }

            if let Some(file) = entry.as_file() {
                if let Some(contents) = file.contents_utf8() {
                    let table = self.lua.load(contents).eval::<Table>()?;

                    let command = LuaCommand {
                        name: table.get("name")?,
                        delay_sec: table
                            .get("delay_sec")
                            .unwrap_or(DEFAULT_COMMAND_DELAY_SEC as u32),
                        options: table.get("options").unwrap_or(DEFAULT_COMMAND_OPTIONS),
                        subcommands: table
                            .get("subcommands")
                            .unwrap_or(DEFAULT_COMMAND_SUBCOMMANDS),
                        minimal_rights: LevelOfRights::from_str(
                            &table
                                .get::<String>("minimal_rights")
                                .unwrap_or("user".into()),
                        ),
                        handle: table.get("handle")?,
                    };

                    log::info!("Successfully loaded \"{}\" command!", &command.name);

                    self.lua_commands.push(command);
                }
            }
        }

        Ok(())
    }
}

#[derive(Clone, Debug)]
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
