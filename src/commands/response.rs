pub enum Response {
    Single(String),
    Multiple(Vec<String>),
}

pub enum ResponseError {
    NotEnoughArguments,
    NoSubcommand,
    NoMessage,
    UnknownSubcommand,

    HttpResponse,

    SomethingWentWrong,
}
