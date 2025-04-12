use std::{error::Error, fmt::Display, str::FromStr, sync::Arc};

use crate::{
    instance_bundle::InstanceBundle,
    localization::LineId,
    modules::{
        chatters::ChattersCommand,
        custom_command::CustomCommandsCommand,
        ecount::EmoteCountCommand,
        esim::EmoteSimilarityCommand,
        etop::EmoteTopCommand,
        event::EventCommand,
        help::HelpCommand,
        holiday::HolidayCommand,
        join::JoinCommand,
        lua::{LuaExecutionCommand, LuaImportCommand},
        massping::MasspingCommand,
        mcsrv::MinecraftServerCommand,
        notify::NotifyCommand,
        ping::PingCommand,
        settings::SettingsCommand,
        spam::SpamCommand,
        timer::TimerCommand,
        userid::UserIdCommand,
    },
    shared_variables::{
        DEFAULT_COMMAND_DELAY_SEC, DEFAULT_COMMAND_LEVEL_OF_RIGHTS, DEFAULT_COMMAND_OPTIONS,
        DEFAULT_COMMAND_SUBCOMMANDS,
    },
};
use async_trait::async_trait;
use common::models::LevelOfRights;
use include_dir::Dir;
use mlua::{Function, Lua, Table, Value};

use self::{
    request::Request,
    response::{Response, ResponseError},
};

use tokio::sync::Mutex;

pub mod lua;
pub mod request;
pub mod response;

const MODULE_DIRECTORY_PATH: &str = "./modules";
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
                Box::new(LuaExecutionCommand),
                Box::new(LuaImportCommand),
            ],
            lua_commands: Vec::new(),
            lua: Lua::new(),
            instance_bundle,
        }
    }

    pub async fn load(&mut self) -> mlua::Result<()> {
        log::info!("Loading Lua API...");
        lua::register_lua_functions(&self.lua, &self.instance_bundle)?;

        log::info!("Loading Lua commands...");

        #[cfg(not(debug_assertions))]
        self.load_directory(&MODULE_DIRECTORY)?;

        #[cfg(debug_assertions)]
        self.load_directory(MODULE_DIRECTORY_PATH)?;

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

        lua::setup_lua_compiler(&self.lua).expect("Error setting up Lua compiler");

        match command
            .handle
            .call_async::<Value>(
                request
                    .as_lua_table(&self.lua)
                    .expect("Error converting Request to Table"),
            )
            .await
        {
            Ok(v) => match v {
                Value::String(v) => Ok(Response::Single(v.to_string_lossy())),
                Value::Table(t) => {
                    if let Some(error) = ResponseError::from_lua_table(&t) {
                        return Err(error);
                    }

                    Ok(Response::Multiple(
                    t.sequence_values::<String>()
                        .collect::<mlua::Result<_>>()
                        .expect("Error collecting table"),
                    ))
                }
                _ => Err(ResponseError::LuaUnsupportedResponseType(
                    v.type_name().to_string(),
                )),
            },
            Err(e) => Err(ResponseError::LuaExecutionError(e)),
        }
    }

    #[cfg(not(debug_assertions))]
    fn load_directory(&mut self, dir: &Dir<'_>) -> mlua::Result<()> {
        for entry in dir.entries() {
            if let Some(dir) = entry.as_dir() {
                self.load_directory(dir)?;
                continue;
            }

            if let Some(file) = entry.as_file() {
                if let Some(contents) = file.contents_utf8() {
                    lua::setup_lua_compiler(&self.lua).expect("Error setting up Lua compiler");
                    let table = self.lua.load(contents).eval::<Table>()?;
                    self.load_lua_command(&table)?;
                }
            }
        }

        Ok(())
    }

    #[cfg(debug_assertions)]
    fn load_directory(&mut self, dir: &str) -> mlua::Result<()> {
        for entry in std::fs::read_dir(dir)? {
            let entry = entry?;
            let path = entry.path();

            if path.is_dir() {
                self.load_directory(path.to_str().unwrap())?;
                continue;
            }

            let contents = std::fs::read_to_string(path)?;
            lua::setup_lua_compiler(&self.lua).expect("Error setting up Lua compiler");
            let table = self.lua.load(contents).eval::<Table>()?;
            self.load_lua_command(&table)?;
        }

        Ok(())
    }

    fn load_lua_command(&mut self, table: &Table) -> mlua::Result<()> {
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

        Ok(())
    }

    #[cfg(debug_assertions)]
    pub fn enable_hot_reloading(loader: Arc<Mutex<Self>>) {
        use notify::{Config, RecommendedWatcher, Watcher};
        use std::{
            collections::HashMap,
            path::{Path, PathBuf},
            sync::mpsc::channel,
            time::{Duration, Instant},
        };

        tokio::spawn({
            let loader = loader.clone();
            async move {
                log::info!("Listening for changes in Lua modules...");

                let (tx, rx) = channel();
                let mut watcher = RecommendedWatcher::new(
                    tx,
                    Config::default()
                        .with_poll_interval(Duration::from_secs(2))
                        .with_compare_contents(true),
                )
                .expect("Error creating file watcher");

                watcher
                    .watch(
                        Path::new(MODULE_DIRECTORY_PATH),
                        notify::RecursiveMode::Recursive,
                    )
                    .expect("Error watching files");

                let mut last_files: HashMap<PathBuf, Instant> = HashMap::new();

                for res in rx {
                    let Ok(event) = res else {
                        continue;
                    };

                    if event.kind.is_modify() || event.kind.is_create() || event.kind.is_remove() {
                        let now = Instant::now();
                        let mut reload = false;

                        for path in event.paths {
                            reload = match last_files.get(&path) {
                                Some(n) => now.duration_since(*n) > Duration::from_millis(200),
                                None => true,
                            };

                            last_files.insert(path, now.clone());

                            if reload {
                                break;
                            }
                        }

                        if reload {
                            log::info!("Reloading Lua commands...");

                            let mut loader = loader.lock().await;
                            loader.lua_commands.clear();

                            match loader.load_directory(MODULE_DIRECTORY_PATH) {
                                Ok(_) => log::info!("Reloaded!"),
                                Err(e) => log::error!("Error reloading Lua commands: {}", e),
                            }
                        }
                    }
                }
            }
        });
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

impl FromStr for CommandArgument {
    type Err = Box<dyn Error>;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "subcommand" => Ok(Self::Subcommand),
            "message" => Ok(Self::Message),
            "interval" => Ok(Self::Interval),
            "name" => Ok(Self::Name),
            "target" => Ok(Self::Target),
            "value" => Ok(Self::Amount),
            _ => Err(format!("Failed to parse CommandArgument from {}", s).into()),
        }
    }
}

impl Display for CommandArgument {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{}",
            match &self {
                Self::Subcommand => "subcommand",
                Self::Message => "message",
                Self::Interval => "interval",
                Self::Name => "name",
                Self::Target => "target",
                Self::Value => "value",
                Self::Amount => "amount",
            }
        )
    }
}
