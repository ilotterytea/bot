use async_trait::async_trait;

#[async_trait]
pub trait Command {
    fn get_name(&self) -> String;
    async fn execute(&self) -> Option<Vec<String>>;
}

pub struct CommandLoader {
    pub commands: Vec<Box<dyn Command + Send + Sync>>,
}

impl CommandLoader {
    pub fn new() -> Self {
        Self { commands: vec![] }
    }

    pub async fn execute_command(&self, id: &str) -> Result<Option<Vec<String>>, &str> {
        if let Some(command) = self.commands.iter().find(|x| x.get_name().eq(id)) {
            return Ok(command.execute().await);
        }
        Err("bruh")
    }
}
