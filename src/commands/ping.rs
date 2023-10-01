use async_trait::async_trait;

use crate::{
    command::Command, instance_bundle::InstanceBundle, localization::LineId,
    message::ParsedPrivmsgMessage,
};

pub struct PingCommand;

#[async_trait]
impl Command for PingCommand {
    fn get_name(&self) -> String {
        "ping".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        message: ParsedPrivmsgMessage,
    ) -> Option<Vec<String>> {
        Some(vec![instance_bundle
            .localizator
            .get_formatted_text("english", LineId::CommandPingResponse, vec![])
            .unwrap()])
    }
}
