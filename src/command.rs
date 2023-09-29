use async_trait::async_trait;

#[async_trait]
pub trait Command {
    fn get_name(&self) -> String;
    async fn execute(&self) -> Option<Vec<String>>;
}
