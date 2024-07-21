use std::env;

use async_trait::async_trait;
use diesel::{insert_into, ExpressionMethods, QueryDsl, RunQueryDsl};
use log::warn;
use serde::Serialize;
use twitch_api::{
    helix::users::GetUsersRequest,
    types::{NicknameRef, UserIdRef},
};

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
};

use common::{
    establish_connection,
    models::{Channel, NewChannel, NewChannelPreference},
    schema::{channel_preferences::dsl as chp, channels::dsl as ch},
};

pub struct JoinCommand;

#[derive(Serialize)]
struct StatsAPIJoinBody {
    pub twitch_id: u32,
}

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
        let superuser_alias_id = env::var("BOT_OWNER_ID");

        let (alias_id, alias_name): (i32, String) = if request.message.is_some()
            && superuser_alias_id.is_ok()
            && superuser_alias_id
                .unwrap()
                .eq(&request.sender.alias_id.to_string())
        {
            let msg = request.message.clone().unwrap();

            let ids: &[&UserIdRef] = &[msg.as_str().into()];
            let logins: &[&NicknameRef] = &[NicknameRef::from_str(msg.as_str())];

            let req = if msg.parse::<u32>().is_ok() {
                GetUsersRequest::ids(ids)
            } else {
                GetUsersRequest::logins(logins)
            };

            let users = instance_bundle
                .twitch_api_client
                .req_get(req, &*instance_bundle.twitch_api_token)
                .await
                .expect("Failed to get users")
                .data;

            match users.first() {
                Some(user) => (
                    user.id.clone().take().parse::<i32>().unwrap(),
                    user.login.clone().take(),
                ),
                None => return Err(ResponseError::NotFound(msg)),
            }
        } else {
            (request.sender.alias_id, request.sender.alias_name.clone())
        };

        let conn = &mut establish_connection();

        let channel_query = ch::channels
            .filter(ch::alias_id.eq(&alias_id))
            .get_result::<Channel>(conn);

        if channel_query.is_ok() {
            return Ok(Response::Single(
                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandJoinAlreadyJoined,
                    Vec::<String>::new(),
                ),
            ));
        }

        insert_into(ch::channels)
            .values([NewChannel {
                alias_id,
                alias_name: alias_name.clone(),
            }])
            .execute(conn)
            .expect("Failed to insert a new channel");

        let new_channel = ch::channels
            .filter(ch::alias_id.eq(&alias_id))
            .first::<Channel>(conn)
            .expect("Failed to get users");

        insert_into(chp::channel_preferences)
            .values([NewChannelPreference {
                channel_id: new_channel.id,
                prefix: request.channel_preference.prefix.clone(),
                language: request.channel_preference.language.clone(),
            }])
            .execute(conn)
            .expect("Failed to insert preferences for a new channel");

        instance_bundle
            .twitch_irc_client
            .join(alias_name.clone())
            .expect("Failed to join chat room");

        instance_bundle
            .twitch_irc_client
            .say(
                alias_name.clone(),
                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::CommandJoinResponseInChat,
                    Vec::<String>::new(),
                ),
            )
            .await
            .expect("Failed to send a message");

        if let Ok(stats_hostname) = env::var("STATS_API_HOSTNAME") {
            let url = format!("{}/api/v1/join", stats_hostname);

            let client = reqwest::Client::new();
            let mut req = client.post(url).json(&StatsAPIJoinBody {
                twitch_id: alias_id as u32,
            });

            if let Ok(credentials) = env::var("STATS_API_PASSWORD") {
                let mut split = credentials.split(':').collect::<Vec<&str>>();

                if !split.is_empty() {
                    let name = split[0];
                    split.remove(0);

                    let password = split.join(":");

                    req = req.basic_auth(
                        name,
                        if password.is_empty() {
                            None
                        } else {
                            Some(password)
                        },
                    );
                }
            }

            if let Ok(res) = req.send().await {
                if res.status() != reqwest::StatusCode::OK {
                    warn!("Failed to channel alias ID {} to join Stats API!", alias_id);
                }
            }
        }

        Ok(Response::Single(
            instance_bundle.localizator.formatted_text_by_request(
                &request,
                LineId::CommandJoinResponse,
                Vec::<String>::new(),
            ),
        ))
    }
}
