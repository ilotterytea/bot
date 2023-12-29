use crate::localization::LineId;

pub enum Response {
    Single(String),
    Multiple(Vec<String>),
}

#[derive(Debug)]
pub enum ResponseError {
    NotEnoughArguments,
    NoSubcommand,
    NoMessage,
    UnknownSubcommand,

    HttpResponse,

    SomethingWentWrong,

    WrongArguments,
    ExternalAPIError(u32, Option<String>),

    Custom(LineId),
}
