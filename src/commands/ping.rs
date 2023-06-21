use crate::api::command::Command;
use crate::api::message::ParsedMessage;
use crate::locale::{LineId, Localizations};
use crate::shared_variables::START_TIME;
use crate::utils::format_timestamp;
use async_trait::async_trait;
use psutil::process::processes;
use std::time::Instant;
use twitch_irc::message::PrivmsgMessage;
use version_check::Version;

pub struct PingCommand;

#[async_trait]
impl Command for PingCommand {
    fn get_name_id(&self) -> String {
        "ping".to_string()
    }

    async fn run(
        &self,
        _event_message: &PrivmsgMessage,
        _message: ParsedMessage,
    ) -> Option<Vec<String>> {
        let current_time = Instant::now();
        let elapsed_time = current_time - *START_TIME;

        let all_processes = processes().expect("Failed to get processes");

        let process = all_processes
            .into_iter()
            .find(|p| p.as_ref().unwrap().pid() == std::process::id())
            .expect("Failed to find current process")
            .ok()?;

        let memory_info = process.memory_info().expect("Failed to get memory info");
        let used_memory_mb = ((memory_info.rss() as f32 / 1024.0) / 1024.0).round();

        let version = match Version::read() {
            Some(d) => d.to_string(),
            None => "N/A".to_string(),
        };

        Some(vec![Localizations::formatted_text(
            "english",
            LineId::CMD_PING_RESPONSE,
            vec![
                version,
                format_timestamp(elapsed_time.as_secs()),
                used_memory_mb.to_string(),
            ],
        )
        .expect("no")])
    }
}
