use async_trait::async_trait;
use common::format_timestamp;
use eyre::Result;
use psutil::process::processes;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    shared_variables::{COMPILE_TIMESTAMP, COMPILE_VERSION, START_TIME},
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
        request: Request,
    ) -> Result<Response, ResponseError> {
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

        let compile_timestamp = chrono::Utc::now().timestamp() - COMPILE_TIMESTAMP as i64;
        let compile_timestamp = format_timestamp(compile_timestamp as u64);

        Ok(Response::Single(
            instance_bundle.localizator.formatted_text_by_request(
                &request,
                LineId::CommandPingResponse,
                vec![
                    COMPILE_VERSION.to_string(),
                    format_timestamp(uptime),
                    if used_memory_mb > -1.0 {
                        used_memory_mb.to_string()
                    } else {
                        "N/A".to_string()
                    },
                    compile_timestamp,
                    env!("CARGO_PKG_VERSION").to_string(),
                ],
            ),
        ))
    }
}
