use async_trait::async_trait;

use crate::{
    commands::{
        Command,
        request::Request,
        response::{Response, ResponseError},
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
};

pub struct HelpCommand;

#[async_trait]
impl Command for HelpCommand {
    fn get_name(&self) -> String {
        "help".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let url = instance_bundle.configuration.third_party.docs_url.clone();

        if let Some(command_id) = request.message.clone() {
            if let Some(command_line_id) = LineId::from_string(format!("hint.url.{}", command_id)) {
                if let Some(command_line) = instance_bundle.localizator.get_literal_text(
                    request.channel_preference.language.as_str(),
                    command_line_id,
                ) {
                    return Ok(Response::Single(
                        instance_bundle.localizator.formatted_text_by_request(
                            &request,
                            LineId::HelpCommand,
                            vec![
                                request.channel_preference.prefix.clone(),
                                command_id,
                                url,
                                command_line,
                            ],
                        ),
                    ));
                }
            }
        }

        return Ok(Response::Single(
            instance_bundle.localizator.formatted_text_by_request(
                &request,
                LineId::HelpGeneral,
                vec![url],
            ),
        ));
    }
}
