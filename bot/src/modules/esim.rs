use async_trait::async_trait;

use crate::{
    commands::{
        Command,
        request::Request,
        response::{Response, ResponseError},
    },
    instance_bundle::InstanceBundle,
};

pub struct EmoteSimilarityCommand;

#[async_trait]
impl Command for EmoteSimilarityCommand {
    fn get_name(&self) -> String {
        "esim".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        // TODO: this command will have a logic based on twitch_emotes.
        Err(ResponseError::NotRegisteredCommand("esim".into()))
    }
}
