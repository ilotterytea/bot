use std::time::Instant;

use async_trait::async_trait;
use psutil::process::processes;
use twitch_irc::message::PrivmsgMessage;
use version_check::Version;

use crate::{
    command::Command, instance_bundle::InstanceBundle, localization::LineId,
    message::ParsedPrivmsgMessage, shared_variables::START_TIME, utils::format_timestamp,
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
        data_message: PrivmsgMessage,
        message: ParsedPrivmsgMessage,
    ) -> Option<Vec<String>> {
        let rust_version = match Version::read() {
            Some(version) => version.to_string(),
            None => "N/A".to_string(),
        };

        // Getting uptime
        let uptime = START_TIME.elapsed().as_secs();

        // Getting process information
        let processes = processes().expect("Failed to get system processes");

        let process_id = std::process::id();
        let used_memory = if let Some(Ok(process)) = processes
            .into_iter()
            .find(|x| x.as_ref().unwrap().pid() == process_id)
        {
            if let Ok(memory) = process.memory_info() {
                memory.rss() as i64
            } else {
                -1
            }
        } else {
            -1
        } as f64;

        let used_memory_mb = if used_memory > -1.0 {
            ((used_memory / 1024.0) / 1024.0).round()
        } else {
            -1.0
        };

        Some(vec![instance_bundle
            .localizator
            .get_formatted_text(
                "english",
                LineId::CommandPingResponse,
                vec![
                    data_message.sender.name,
                    rust_version,
                    format_timestamp(uptime),
                    if used_memory_mb > -1.0 {
                        used_memory_mb.to_string()
                    } else {
                        "N/A".to_string()
                    },
                ],
            )
            .unwrap()])
    }
}
