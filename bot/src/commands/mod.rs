use std::sync::Arc;

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
use chrono::Utc;
use common::{establish_connection, format_timestamp, models::LevelOfRights};
use eyre::Result;
use include_dir::Dir;
use mlua::{Function, Lua, LuaSerdeExt, Table, Value, VmState};
use tokio::time::Instant;

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
        register_lua_functions(&self.lua, &self.instance_bundle)?;

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

        setup_lua_compiler(&self.lua).expect("Error setting up Lua compiler");

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

    fn load_directory(&mut self, dir: &Dir<'_>) -> mlua::Result<()> {
        for entry in dir.entries() {
            if let Some(dir) = entry.as_dir() {
                self.load_directory(dir)?;
                continue;
            }

            if let Some(file) = entry.as_file() {
                if let Some(contents) = file.contents_utf8() {
                    setup_lua_compiler(&self.lua).expect("Error setting up Lua compiler");

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

pub fn register_lua_functions(lua: &Lua, instance_bundle: &InstanceBundle) -> mlua::Result<()> {
    // --- BOT FUNCTIONS ---
    let l10n_formatted_text_request = lua.create_function({
        let localizator = instance_bundle.localizator.clone();
        move |_, (request, line_id, parameters): (Table, String, Table)| {
            let request = Request::from_lua_table(request)?;
            let Some(line_id) = LineId::from_string(line_id.clone()) else {
                return Err(mlua::Error::RuntimeError(format!(
                    "Unknown line ID: {}",
                    line_id
                )));
            };
            let parameters = parameters
                .sequence_values()
                .flatten()
                .collect::<Vec<String>>();

            Ok(localizator.formatted_text_by_request(&request, line_id, parameters))
        }
    })?;
    lua.globals()
        .set("l10n_formatted_text_request", l10n_formatted_text_request)?;

    // --- LUA FUNCTIONS ---
    let print = lua.create_function(|_, ()| Ok(()))?;
    lua.globals().set("print", print)?;

    register_lua_json_functions(lua)?;
    register_lua_time_functions(lua)?;

    Ok(())
}

pub fn setup_lua_compiler(lua: &Lua) -> mlua::Result<()> {
    lua.sandbox(true)?;
    lua.set_memory_limit(1024 * 1024 * 2)?;

    let now = Instant::now();

    lua.set_interrupt(move |_| {
        if now.elapsed().as_millis() >= 600 {
            return Ok(VmState::Yield);
        }
        Ok(VmState::Continue)
    });

    Ok(())
}

pub fn register_lua_storage_functions(
    lua: &Lua,
    paste_id: String,
    user_id: i32,
) -> mlua::Result<()> {
    use common::{
        models::{LuaStorage, NewLuaStorage},
        schema::lua_storage::dsl as luas,
    };
    use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};

    let storage_get = lua.create_function({
        let paste_id = paste_id.clone();
        let user_id = user_id.clone();
        move |_, ()| {
            let conn = &mut establish_connection();
            let storage: LuaStorage = luas::lua_storage
                .filter(luas::user_id.eq(&user_id))
                .filter(luas::lua_id.eq(&paste_id))
                .get_result(conn)
                .unwrap_or_else(|_| {
                    diesel::insert_into(luas::lua_storage)
                        .values(NewLuaStorage {
                            user_id: user_id.clone(),
                            lua_id: paste_id.clone(),
                        })
                        .get_result::<LuaStorage>(conn)
                        .expect("Error creating new Lua storage")
                });

            Ok(storage.value)
        }
    })?;
    lua.globals().set("storage_get", storage_get)?;

    let storage_put = lua.create_function({
        let paste_id = paste_id.clone();
        let user_id = user_id.clone();
        move |_, value: String| {
            let conn = &mut establish_connection();
            let storage: LuaStorage = luas::lua_storage
                .filter(luas::user_id.eq(&user_id))
                .filter(luas::lua_id.eq(&paste_id))
                .get_result(conn)
                .unwrap_or_else(|_| {
                    diesel::insert_into(luas::lua_storage)
                        .values(NewLuaStorage {
                            user_id: user_id.clone(),
                            lua_id: paste_id.clone(),
                        })
                        .get_result::<LuaStorage>(conn)
                        .expect("Error creating new Lua storage")
                });

            Ok(diesel::update(luas::lua_storage.find(&storage.id))
                .set(luas::value.eq(value))
                .execute(conn)
                .is_ok())
        }
    })?;
    lua.globals().set("storage_put", storage_put)?;

    Ok(())
}

pub fn register_lua_json_functions(lua: &Lua) -> mlua::Result<()> {
    let json_parse = lua.create_function({
        let lua = lua.clone();
        move |_, value: String| {
            let Ok(json_value) = serde_json::from_str::<serde_json::Value>(&value) else {
                return Err(mlua::Error::RuntimeError(
                    "Error converting string to JSON".into(),
                ));
            };

            let lua_value = lua.to_value(&json_value)?;

            Ok(lua_value)
        }
    })?;
    lua.globals().set("json_parse", json_parse)?;

    let json_stringify = lua.create_function({
        let lua = lua.clone();
        move |_, value: Value| {
            let json: serde_json::Value = lua.from_value(value)?;

            let Ok(string) = serde_json::to_string(&json) else {
                return Err(mlua::Error::RuntimeError(
                    "Error converting Lua value to Stringified JSON".into(),
                ));
            };

            Ok(string)
        }
    })?;
    lua.globals().set("json_stringify", json_stringify)?;

    Ok(())
}

pub fn register_lua_time_functions(lua: &Lua) -> mlua::Result<()> {
    let time_current = lua.create_function(|_, ()| Ok(Utc::now().timestamp_millis()))?;
    lua.globals().set("time_current", time_current)?;

    let time_humanize = lua.create_function(|_, timestamp_in_seconds: u64| {
        Ok(format_timestamp(timestamp_in_seconds))
    })?;
    lua.globals().set("time_humanize", time_humanize)?;

    Ok(())
}
