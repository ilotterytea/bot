use std::{sync::Arc, time::Instant};

use chrono::Utc;
use common::{config::Configuration, establish_connection, format_timestamp};
use mlua::{Lua, LuaSerdeExt, Table, Value, VmState};

use crate::{instance_bundle::InstanceBundle, localization::LineId};

use super::{request::Request, response::ResponseError};

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

    let err = lua.create_function({
        let lua = lua.clone();
        move |_, (name, arguments): (String, Table)| {
            let args = arguments
                .sequence_values()
                .flatten()
                .collect::<Vec<String>>();

            let Some(error) = ResponseError::from_str_and_args(&name, &args) else {
                return Err(mlua::Error::RuntimeError(format!(
                    "Failed to parse ResponseError from {} and args -> {}",
                    name,
                    args.join(", ")
                )));
            };

            error.to_lua_table(&lua)
        }
    })?;
    lua.globals().set("err", err)?;

    // --- LUA FUNCTIONS ---
    let print = lua.create_function(|_, ()| Ok(()))?;
    lua.globals().set("print", print)?;

    register_lua_json_functions(lua)?;
    register_lua_str_functions(lua)?;
    register_lua_time_functions(lua)?;
    register_lua_bot_functions(lua, instance_bundle.configuration.clone())?;

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
    channel_id: i32,
) -> mlua::Result<()> {
    use common::{
        models::{LuaChannelStorage, LuaUserStorage, NewLuaChannelStorage, NewLuaUserStorage},
        schema::{lua_channel_storage::dsl as luach, lua_user_storage::dsl as luaus},
    };
    use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};

    let storage_get = lua.create_function({
        let paste_id = paste_id.clone();
        move |_, ()| {
            let conn = &mut establish_connection();
            let storage: LuaUserStorage = luaus::lua_user_storage
                .filter(luaus::user_id.eq(&user_id))
                .filter(luaus::lua_id.eq(&paste_id))
                .get_result(conn)
                .unwrap_or_else(|_| {
                    diesel::insert_into(luaus::lua_user_storage)
                        .values(NewLuaUserStorage {
                            user_id,
                            lua_id: paste_id.clone(),
                        })
                        .get_result::<LuaUserStorage>(conn)
                        .expect("Error creating new Lua storage")
                });

            Ok(storage.value)
        }
    })?;
    lua.globals().set("storage_get", storage_get)?;

    let storage_put = lua.create_function({
        let paste_id = paste_id.clone();
        move |_, value: String| {
            let conn = &mut establish_connection();
            let storage: LuaUserStorage = luaus::lua_user_storage
                .filter(luaus::user_id.eq(&user_id))
                .filter(luaus::lua_id.eq(&paste_id))
                .get_result(conn)
                .unwrap_or_else(|_| {
                    diesel::insert_into(luaus::lua_user_storage)
                        .values(NewLuaUserStorage {
                            user_id,
                            lua_id: paste_id.clone(),
                        })
                        .get_result::<LuaUserStorage>(conn)
                        .expect("Error creating new Lua storage")
                });

            Ok(diesel::update(luaus::lua_user_storage.find(&storage.id))
                .set(luaus::value.eq(value))
                .execute(conn)
                .is_ok())
        }
    })?;
    lua.globals().set("storage_put", storage_put)?;

    let storage_channel_get = lua.create_function({
        let paste_id = paste_id.clone();
        move |_, ()| {
            let conn = &mut establish_connection();
            let storage: LuaChannelStorage = luach::lua_channel_storage
                .filter(luach::channel_id.eq(&channel_id))
                .filter(luach::lua_id.eq(&paste_id))
                .get_result(conn)
                .unwrap_or_else(|_| {
                    diesel::insert_into(luach::lua_channel_storage)
                        .values(NewLuaChannelStorage {
                            channel_id,
                            lua_id: paste_id.clone(),
                        })
                        .get_result::<LuaChannelStorage>(conn)
                        .expect("Error creating new Lua channel storage")
                });

            Ok(storage.value)
        }
    })?;
    lua.globals()
        .set("storage_channel_get", storage_channel_get)?;

    let storage_channel_put = lua.create_function({
        let paste_id = paste_id.clone();
        move |_, value: String| {
            let conn = &mut establish_connection();
            let storage: LuaChannelStorage = luach::lua_channel_storage
                .filter(luach::channel_id.eq(&channel_id))
                .filter(luach::lua_id.eq(&paste_id))
                .get_result(conn)
                .unwrap_or_else(|_| {
                    diesel::insert_into(luach::lua_channel_storage)
                        .values(NewLuaChannelStorage {
                            channel_id,
                            lua_id: paste_id.clone(),
                        })
                        .get_result::<LuaChannelStorage>(conn)
                        .expect("Error creating new Lua channel storage")
                });

            Ok(diesel::update(luach::lua_channel_storage.find(&storage.id))
                .set(luach::value.eq(value))
                .execute(conn)
                .is_ok())
        }
    })?;
    lua.globals()
        .set("storage_channel_put", storage_channel_put)?;

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

pub fn register_lua_str_functions(lua: &Lua) -> mlua::Result<()> {
    let str_split = lua.create_function(|_, (value, delimiter): (String, String)| {
        Ok(value
            .split(&delimiter)
            .map(|x| x.to_string())
            .collect::<Vec<String>>())
    })?;
    lua.globals().set("str_split", str_split)?;

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

pub fn register_lua_bot_functions(
    lua: &Lua,
    configuration: Arc<Configuration>,
) -> mlua::Result<()> {
    let bot_get_compiler_version =
        lua.create_function(|_, ()| Ok(compile_time::rustc_version_str!()))?;
    lua.globals()
        .set("bot_get_compiler_version", bot_get_compiler_version)?;

    let bot_get_uptime = lua.create_function(|_, ()| {
        let uptime = std::env::var("BOT_START_TIMESTAMP")
            .expect("BOT_START_TIMESTAMP must be set for uptime calculations")
            .parse::<i64>()
            .unwrap();

        Ok(chrono::Utc::now().naive_utc().and_utc().timestamp() - uptime)
    })?;
    lua.globals().set("bot_get_uptime", bot_get_uptime)?;

    let bot_get_memory_usage = lua.create_function(|_, ()| Ok(0))?;
    lua.globals()
        .set("bot_get_memory_usage", bot_get_memory_usage)?;

    let bot_get_compile_time = lua.create_function(|_, ()| Ok(compile_time::unix!()))?;
    lua.globals()
        .set("bot_get_compile_time", bot_get_compile_time)?;

    let bot_get_version = lua.create_function(|_, ()| Ok(env!("CARGO_PKG_VERSION")))?;
    lua.globals().set("bot_get_version", bot_get_version)?;

    let bot_config = lua.create_function({
        let lua = lua.clone();
        let configuration = configuration.clone();
        move |_, ()| {
            let table = lua.create_table()?;

            let bot_table = lua.create_table()?;
            bot_table.set("owner_twitch_id", configuration.bot.owner_twitch_id)?;
            table.set("bot", bot_table)?;

            let commands_table = lua.create_table()?;
            commands_table.set(
                "default_prefix",
                configuration.commands.default_prefix.clone(),
            )?;
            commands_table.set(
                "default_language",
                configuration.commands.default_language.clone(),
            )?;
            commands_table.set("spam", {
                let spam_table = lua.create_table()?;
                spam_table.set("max_count", configuration.commands.spam.max_count)?;
                spam_table
            })?;
            table.set("commands", commands_table)?;

            let third_party_table = lua.create_table()?;
            third_party_table.set("docs_url", configuration.third_party.docs_url.clone())?;
            third_party_table.set(
                "stats_api_url",
                configuration.third_party.stats_api_url.clone(),
            )?;
            third_party_table.set(
                "pastea_api_url",
                configuration.third_party.pastea_api_url.clone(),
            )?;
            table.set("third_party", third_party_table)?;

            Ok(table)
        }
    })?;
    lua.globals().set("bot_config", bot_config)?;

    Ok(())
}
