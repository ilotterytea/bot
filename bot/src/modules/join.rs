use async_trait::async_trait;
use diesel::{insert_into, ExpressionMethods, QueryDsl, RunQueryDsl};

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    shared_variables::{DEFAULT_LANGUAGE, DEFAULT_PREFIX},
};

use common::{
    establish_connection,
    models::{Channel, NewChannel, NewChannelPreference},
    schema::{channel_preferences::dsl as chp, channels::dsl as ch},
};

pub struct JoinCommand;

#[async_trait]
impl Command for JoinCommand {
    fn get_name(&self) -> String {
        "join".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        if request.message.is_some() {
            return Ok(Response::Single(
                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::JoinOtherchatroom,
                    Vec::<String>::new(),
                ),
            ));
        }

        let conn = &mut establish_connection();

        let channel_query = ch::channels
            .filter(ch::alias_id.eq(request.sender.alias_id))
            .load::<Channel>(conn)
            .expect("Failed to get users");

        if !channel_query.is_empty() {
            return Ok(Response::Single(
                instance_bundle
                    .localizator
                    .formatted_text_by_request(
                        &request,
                        LineId::CommandJoinResponse,
                        Vec::<String>::new(),
                    ),
            ));
        }

        insert_into(ch::channels)
            .values([NewChannel {
                alias_id: request.sender.alias_id,
                alias_name: request.sender.alias_name.clone(),
            }])
            .execute(conn)
            .expect("Failed to insert a new channel");

        let new_channel = ch::channels
            .filter(ch::alias_id.eq(request.sender.alias_id))
            .first::<Channel>(conn)
            .expect("Failed to get users");

        insert_into(chp::channel_preferences)
            .values([NewChannelPreference {
                channel_id: new_channel.id,
                prefix: DEFAULT_PREFIX.to_string(),
                language: DEFAULT_LANGUAGE.to_string(),
            }])
            .execute(conn)
            .expect("Failed to insert preferences for a new channel");

        instance_bundle
            .twitch_irc_client
            .join(request.sender.alias_name.clone())
            .expect("Failed to join chat room");

        instance_bundle
            .twitch_irc_client
            .say(
                request.sender.alias_name.clone(),
                instance_bundle
                    .localizator
                    .formatted_text_by_request(
                        &request,
                        LineId::CommandJoinResponseInChat,
                        Vec::<String>::new(),
                    ),
            )
            .await
            .expect("Failed to send a message");

        Ok(Response::Single(
            instance_bundle
                .localizator
                .formatted_text_by_request(
                    &request,
                    LineId::CommandJoinResponse,
                    Vec::<String>::new(),
                )
        ))
    }
}
