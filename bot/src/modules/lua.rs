use std::{sync::mpsc, time::Duration};

use async_trait::async_trait;
use mlua::{Function, Lua, Value};
use reqwest::{Client, StatusCode};
use substring::Substring;

use crate::{
    commands::{
        register_lua_functions, register_lua_storage_functions,
        request::Request,
        response::{Response, ResponseError},
        setup_lua_compiler, Command, CommandArgument,
    },
    instance_bundle::InstanceBundle,
};

fn beautify_str(s: String) -> String {
    let mut s = format!("🌑 {}", s);

    if s.len() > 200 {
        s = s.substring(0, 197).to_string();
        s.push_str("...");
    }

    s
}

fn run_lua_script(lua: Lua, request: &Request, script: String) -> Result<Response, ResponseError> {
    let (tx, rx) = mpsc::channel();

    let _ = std::thread::spawn({
        let code = script.clone();
        let lua = lua.clone();
        move || {
            let result = lua.load(code).eval::<Value>();
            let _ = tx.send(result);
        }
    });

    let time = 500;

    match rx.recv_timeout(Duration::from_millis(time)) {
        Ok(result) => match result {
            Ok(v) => match v {
                Value::String(v) => Ok(Response::Single(beautify_str(v.to_string_lossy()))),
                Value::Integer(i) => Ok(Response::Single(beautify_str(i.to_string()))),
                Value::Boolean(b) => Ok(Response::Single(beautify_str(b.to_string()))),
                Value::Number(n) => Ok(Response::Single(beautify_str(n.to_string()))),
                Value::Nil => Ok(Response::Single(beautify_str("nil".to_string()))),
                Value::Function(f) => run_lua_function(lua, request, f),
                _ => Err(ResponseError::LuaUnsupportedResponseType(
                    v.type_name().to_string(),
                )),
            },
            Err(e) => Err(ResponseError::LuaExecutionError(e)),
        },
        Err(_) => return Err(ResponseError::LuaExceededWaitingTime(time)),
    }
}

fn run_lua_function(
    lua: Lua,
    request: &Request,
    function: Function,
) -> Result<Response, ResponseError> {
    let (tx, rx) = mpsc::channel();

    let _ = std::thread::spawn({
        let request = request
            .as_lua_table(&lua)
            .expect("Error converting Request to Lua table");
        move || {
            let result = function.call(request);
            let _ = tx.send(result);
        }
    });

    let time = 500;

    match rx.recv_timeout(Duration::from_millis(time)) {
        Ok(result) => match result {
            Ok(v) => match v {
                Value::String(v) => Ok(Response::Single(beautify_str(v.to_string_lossy()))),
                Value::Integer(i) => Ok(Response::Single(beautify_str(i.to_string()))),
                Value::Boolean(b) => Ok(Response::Single(beautify_str(b.to_string()))),
                Value::Number(n) => Ok(Response::Single(beautify_str(n.to_string()))),
                Value::Nil => Ok(Response::Single(beautify_str("nil".to_string()))),
                _ => Err(ResponseError::LuaUnsupportedResponseType(
                    v.type_name().to_string(),
                )),
            },
            Err(e) => Err(ResponseError::LuaExecutionError(e)),
        },
        Err(_) => return Err(ResponseError::LuaExceededWaitingTime(time)),
    }
}

pub struct LuaExecutionCommand;

#[async_trait]
impl Command for LuaExecutionCommand {
    fn get_name(&self) -> String {
        "lua".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let Some(code) = &request.message else {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Value));
        };

        let lua = Lua::new();

        if setup_lua_compiler(&lua).is_err()
            || register_lua_functions(&lua, &instance_bundle).is_err()
        {
            return Err(ResponseError::SomethingWentWrong);
        }

        run_lua_script(lua, &request, code.clone())
    }
}

pub struct LuaImportCommand;

#[async_trait]
impl Command for LuaImportCommand {
    fn get_name(&self) -> String {
        "luaimport".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let Some(paste_id) = &request.message else {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Value));
        };

        let parts = paste_id.split(':').collect::<Vec<&str>>();

        if parts.len() != 2 {
            return Err(ResponseError::IncorrectArgument(paste_id.clone()));
        }

        let provider = parts[0];
        let id = parts[1];

        let url = match provider {
            "pastebin" => format!("https://pastebin.com/raw/{}", id),
            _ => return Err(ResponseError::IncorrectArgument(provider.to_string())),
        };

        let paste_id = format!("{}:{}", provider, id);

        let client = Client::new();
        let response = client
            .get(url)
            .send()
            .await
            .expect("Error sending HTTP request");

        if response.status() != StatusCode::OK {
            return Err(ResponseError::ExternalAPIError(
                response.status().as_u16() as u32,
                None,
            ));
        }

        let body = response.text().await.expect("Error reading HTTP text");

        let lua = Lua::new();

        if setup_lua_compiler(&lua).is_err()
            || register_lua_functions(&lua, &instance_bundle).is_err()
            || register_lua_storage_functions(&lua, paste_id, request.sender.id).is_err()
        {
            return Err(ResponseError::SomethingWentWrong);
        }

        run_lua_script(lua, &request, body)
    }
}
