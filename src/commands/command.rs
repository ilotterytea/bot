use super::permissions::Permissions;

pub struct CommandData {
    pub id: String,
    pub delay: usize,
    pub permissions: Permissions,
    pub options: Vec<String>,
    pub subcommands: Vec<String>,
    pub aliases: Vec<String>,
    pub run: fn() -> Option<String>,
}

pub trait CommandBehavior {
    fn new() -> Self;
    fn run() -> Option<String>;
}
