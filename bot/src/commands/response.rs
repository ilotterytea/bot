use std::{fmt::Display, str::FromStr, sync::Arc};

use mlua::{Lua, Table};

use crate::localization::{LineId, Localizator};

use super::{CommandArgument, request::Request};

#[derive(Clone, Debug)]
pub enum Response {
    Single(String),
    Multiple(Vec<String>),
}

impl Display for Response {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Response::Single(v) => write!(f, "{}", v),
            Response::Multiple(v) => {
                write!(
                    f,
                    "[{}]",
                    v.iter()
                        .map(|x| format!("\"{}\"", x))
                        .collect::<Vec<String>>()
                        .join(", ")
                )
            }
        }
    }
}

#[derive(Clone, Debug)]
pub enum ResponseError {
    NotEnoughArguments(CommandArgument),
    WrongArgumentType(String, String),
    IncorrectArgument(String),

    IncompatibleName(String),
    NamesakeCreation(String),
    NotFound(String),

    SomethingWentWrong,

    ExternalAPIError(u32, Option<String>),
    InsufficientRights,

    NotRegisteredCommand(String),
    LuaExecutionError(mlua::Error),
    LuaUnsupportedResponseType(String),
    LuaExceededWaitingTime(u64),
}

impl Display for ResponseError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{}",
            match &self {
                Self::NotEnoughArguments(a) => format!("ResponseError::NotEnoughArguments({})", a),
                Self::WrongArgumentType(a, b) =>
                    format!("ResponseError::WrongArgumentType({}, {})", a, b),
                Self::IncorrectArgument(a) => format!("ResponseError::IncorrectArgument({})", a),
                Self::IncompatibleName(a) => format!("ResponseError::IncompatibleName({})", a),
                Self::NamesakeCreation(a) => format!("ResponseError::NamesakeCreation({})", a),
                Self::NotFound(a) => format!("ResponseError::NotFound({})", a),
                Self::SomethingWentWrong => "ResponseError::SomethingWentWrong".into(),
                Self::ExternalAPIError(a, b) =>
                    format!("ResponseError::ExternalAPIError({}, {:?})", a, b),
                Self::InsufficientRights => "ResponseError::InsufficientRights".into(),
                Self::NotRegisteredCommand(a) =>
                    format!("ResponseError::NotRegisteredCommand({})", a),
                Self::LuaExecutionError(_) => "ResponseError::LuaExecutionError".into(),
                Self::LuaUnsupportedResponseType(a) =>
                    format!("ResponseError::LuaUnsupportedResponseType({})", a),
                Self::LuaExceededWaitingTime(a) =>
                    format!("ResponseError::LuaExceededWaitingTime({})", a),
            }
        )
    }
}

impl ResponseError {
    pub fn formatted_message(
        &self,
        request: &Request,
        docs_url: String,
        localizator: Arc<Localizator>,
    ) -> String {
        let hint_url = format!("hint.url.{}", request.command_id);
        let hint_url_localed = if let Some(line_id) = LineId::from_string(hint_url) {
            localizator
                .get_literal_text(request.channel_preference.language.as_str(), line_id)
                .unwrap_or_default()
        } else {
            "".to_string()
        };

        let docs_line = localizator.formatted_text_by_request(
            request,
            LineId::MsgHint,
            vec![docs_url, hint_url_localed],
        );

        let mut params: Vec<String> = Vec::new();

        let error_line_id: (u8, LineId) = match self {
            Self::NotEnoughArguments(arg) => {
                params.push(
                    localizator
                        .get_literal_text(
                            request.channel_preference.language.as_str(),
                            arg.to_line_id(),
                        )
                        .unwrap(),
                );
                (0, LineId::ErrorNotEnoughArguments)
            }
            Self::WrongArgumentType(arg, pos) => {
                params.push(arg.clone());
                params.push(pos.clone());
                (1, LineId::ErrorWrongArgumentType)
            }
            Self::IncorrectArgument(arg) => {
                params.push(arg.clone());
                (2, LineId::ErrorIncorrectArgument)
            }

            Self::InsufficientRights => (3, LineId::ErrorInsufficientRights),
            Self::IncompatibleName(arg) => {
                params.push(arg.clone());
                (10, LineId::ErrorIncompatibleName)
            }
            Self::NamesakeCreation(arg) => {
                params.push(arg.clone());
                (11, LineId::ErrorNamesakeCreation)
            }
            Self::NotFound(arg) => {
                params.push(arg.clone());
                (12, LineId::ErrorNotFound)
            }
            Self::ExternalAPIError(code, reason) => {
                params.push(code.to_string());
                params.push(reason.clone().unwrap_or_default());
                (20, LineId::ErrorExternalAPIError)
            }
            Self::NotRegisteredCommand(name) => {
                params.push(name.clone());
                (30, LineId::ErrorNotRegisteredCommand)
            }
            Self::LuaExecutionError(error) => {
                params.push(error.to_string());
                (31, LineId::ErrorLuaExecutionError)
            }
            Self::LuaUnsupportedResponseType(typename) => {
                params.push(typename.to_string());
                (32, LineId::ErrorLuaUnsupportedResponseType)
            }
            Self::LuaExceededWaitingTime(time) => {
                params.push(time.to_string());
                (33, LineId::ErrorLuaExceededWaitingTime)
            }
            _ => (127, LineId::ErrorSomethingWentWrong),
        };

        let error_line = localizator.formatted_text_by_request(request, error_line_id.1, params);

        localizator.formatted_text_by_request(
            request,
            LineId::MsgError,
            vec![error_line_id.0.to_string(), error_line, docs_line],
        )
    }

    pub fn from_lua_table(table: &Table) -> Option<Self> {
        let type_name: String = table.get("type").ok()?;

        if type_name.ne("ResponseError") {
            return None;
        }

        let name: String = table.get("name").ok()?;
        let args: Vec<String> = table.get("arguments").unwrap_or_default();
        Self::from_str_and_args(&name, &args)
    }

    pub fn to_lua_table(&self, lua: &Lua) -> mlua::Result<Table> {
        let table = lua.create_table()?;

        table.set("type", "ResponseError")?;

        let (name, args) = match &self {
            Self::NotEnoughArguments(a) => ("not_enough_arguments", vec![a.to_string()]),
            Self::WrongArgumentType(a, b) => {
                ("wrong_argument_type", vec![a.to_string(), b.to_string()])
            }
            Self::IncorrectArgument(a) => ("incorrect_argument", vec![a.clone()]),
            Self::IncompatibleName(a) => ("incompatible_name", vec![a.clone()]),
            Self::NamesakeCreation(a) => ("namesake_creation", vec![a.clone()]),
            Self::NotFound(a) => ("not_found", vec![a.clone()]),
            Self::SomethingWentWrong => ("something_went_wrong", Vec::new()),
            Self::ExternalAPIError(a, b) => (
                "external_api_error",
                if let Some(b) = &b {
                    vec![a.to_string(), b.clone()]
                } else {
                    vec![a.to_string()]
                },
            ),
            Self::InsufficientRights => ("insufficient_rights", Vec::new()),
            _ => {
                return Err(mlua::Error::RuntimeError(format!(
                    "Cannot convert {} to Lua Table",
                    &self
                )));
            }
        };

        table.set("name", name)?;
        table.set("arguments", args)?;

        Ok(table)
    }

    pub fn from_str_and_args(name: &str, args: &[impl Display]) -> Option<Self> {
        match name {
            "not_enough_arguments" if args.len() == 1 => Some(Self::NotEnoughArguments(
                CommandArgument::from_str(&args[0].to_string()).ok()?,
            )),
            "wrong_argument_type" if args.len() == 2 => Some(Self::WrongArgumentType(
                args[0].to_string(),
                args[1].to_string(),
            )),
            "incorrect_argument" if args.len() == 1 => {
                Some(Self::IncorrectArgument(args[0].to_string()))
            }
            "incompatible_name" if args.len() == 1 => {
                Some(Self::IncompatibleName(args[0].to_string()))
            }
            "namesake_creation" if args.len() == 1 => {
                Some(Self::NamesakeCreation(args[0].to_string()))
            }
            "not_found" if args.len() == 1 => Some(Self::NotFound(args[0].to_string())),
            "something_went_wrong" if args.is_empty() => Some(Self::SomethingWentWrong),
            "external_api_error" if !args.is_empty() => Some(Self::ExternalAPIError(
                args[0].to_string().parse::<u32>().ok()?,
                args.get(1).map(|arg| arg.to_string()),
            )),
            "insufficient_rights" if args.is_empty() => Some(Self::InsufficientRights),
            _ => None,
        }
    }
}
