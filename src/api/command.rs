use async_trait::async_trait;

#[async_trait]
/// The default trait for commands.
pub trait Command {
    /// Get the name ID of the command.
    fn get_name_id(&self) -> String;

    /// Run the command.
    async fn run(&self) -> Option<Vec<String>>;
}