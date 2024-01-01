use async_trait::async_trait;
use eyre::Result;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command, CommandArgument,
    },
    instance_bundle::InstanceBundle,
    models::mcsrv::ServerData,
    shared_variables::MCSRV_API_URL,
};

pub struct MinecraftServerCommand;

#[async_trait]
impl Command for MinecraftServerCommand {
    fn get_name(&self) -> String {
        "mcsrv".to_string()
    }

    async fn execute(
        &self,
        _instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        if let Some(message) = request.message.clone() {
            let url = format!("{}/{}", MCSRV_API_URL, message);

            if let Ok(response) = reqwest::get(url).await {
                if let Ok(data) = response.json::<ServerData>().await {
                    let ban_emoji = "⛔";
                    let ok_emoji = "✅";
                    let mut response = format!(
                        "{} {} ({})",
                        if data.online { ok_emoji } else { ban_emoji },
                        message,
                        data.ip
                    );

                    if let Some(players) = data.players {
                        let string = format!(" | {}/{}", players.online, players.max);
                        response.push_str(string.as_str());
                    }

                    if let Some(motd) = data.motd {
                        if let Some(motd) = motd.get("clean") {
                            let string = format!(" | {}", motd.join(";"));
                            response.push_str(string.as_str());
                        }
                    }

                    if let Some(protocol) = data.protocol {
                        if let Some(version) = protocol.name {
                            let string = format!(" | {}", version);
                            response.push_str(string.as_str());
                        }
                    }

                    return Ok(Response::Single(response));
                } else {
                    return Err(ResponseError::SomethingWentWrong);
                }
            } else {
                return Err(ResponseError::NotFound(message));
            }
        }
        Err(ResponseError::NotEnoughArguments(CommandArgument::Target))
    }
}
