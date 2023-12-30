use std::sync::Arc;

use crate::localization::{LineId, Localizator};

use super::{request::Request, CommandArgument};

pub enum Response {
    Single(String),
    Multiple(Vec<String>),
}

#[derive(Debug)]
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
}

impl ResponseError {
    pub fn formatted_message(&self, request: &Request, localizator: Arc<Localizator>) -> String {
        let hint_url = format!("hint.url.{}", request.command_id);
        let hint_url_localed = if let Some(line_id) = LineId::from_string(hint_url) {
            localizator
                .get_literal_text(request.channel_preference.language.as_str(), line_id)
                .unwrap_or_default()
        } else {
            "".to_string()
        };

        let docs_line = localizator.formatted_text_by_request(
            &request,
            LineId::MsgHint,
            vec![hint_url_localed],
        );

        let mut params: Vec<String> = Vec::new();

        let error_line_id: (u8, LineId) = match self {
            Self::NotEnoughArguments(arg) => (0, arg.to_line_id()),
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
            _ => (127, LineId::ErrorSomethingWentWrong),
        };

        let error_line = localizator.formatted_text_by_request(&request, error_line_id.1, params);

        let error = localizator.formatted_text_by_request(
            &request,
            LineId::MsgError,
            vec![error_line_id.0.to_string(), error_line, docs_line],
        );

        error
    }
}
