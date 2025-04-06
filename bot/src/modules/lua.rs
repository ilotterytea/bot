use std::{sync::mpsc, time::Duration};

use async_trait::async_trait;
use mlua::{Lua, Value};
use substring::Substring;

use crate::{
    commands::{
        register_lua_functions,
        request::Request,
        response::{Response, ResponseError},
        setup_lua_compiler, Command, CommandArgument,
    },
    instance_bundle::InstanceBundle,
};

fn beautify_str(s: String) -> String {
    let mut s = format!("🌑 {}", s);

    if s.len() > 103 {
        s = s.substring(0, 100).to_string();
        s.push_str("...");
    }

    s
}

fn run_lua_script(
    script: String,
    instance_bundle: &InstanceBundle,
) -> Result<Response, ResponseError> {
    let lua = Lua::new();
    if setup_lua_compiler(&lua).is_err() || register_lua_functions(&lua, instance_bundle).is_err() {
        return Err(ResponseError::SomethingWentWrong);
    }

    let (tx, rx) = mpsc::channel();

    let _ = std::thread::spawn({
        let code = script.clone();
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

        run_lua_script(code.clone(), instance_bundle)
    }
}
