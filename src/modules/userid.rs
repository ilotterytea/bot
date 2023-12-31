use async_trait::async_trait;
use eyre::Result;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command, CommandArgument,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    models::IvrUserResponse,
    shared_variables::IVR_API_V2_URL,
};

pub struct UserIdCommand;

#[async_trait]
impl Command for UserIdCommand {
    fn get_name(&self) -> String {
        "userid".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        if let Some(message) = request.message.clone() {
            let message_split = message.split(',').collect::<Vec<&str>>();
            let target_names = message_split
                .iter()
                .filter(|x| x.to_string().parse::<u32>().is_err())
                .map(|x| x.to_string())
                .collect::<Vec<String>>();

            let target_ids = message_split
                .iter()
                .filter(|x| x.to_string().parse::<u32>().is_ok())
                .map(|x| x.to_string())
                .collect::<Vec<String>>();

            let first_part_query = if !target_ids.is_empty() {
                format!("id={}", target_ids.join(","))
            } else if !target_names.is_empty() {
                format!("login={}", target_names.join(","))
            } else {
                "".to_string()
            };

            let second_part_query = if !target_names.is_empty() && !target_ids.is_empty() {
                format!("&login={}", target_names.join(","))
            } else {
                "".to_string()
            };

            let url = format!(
                "{}/twitch/user?{}{}",
                IVR_API_V2_URL, first_part_query, second_part_query
            );

            if let Ok(response) = reqwest::get(url).await {
                if let Ok(data) = response.json::<Vec<IvrUserResponse>>().await {
                    if data.is_empty() {
                        return Err(ResponseError::NotFound(message));
                    }

                    //let ids = data
                    //    .iter()
                    //    .map(|x| UserIdRef::from_str(x.id.as_str()))
                    //    .collect::<Vec<&UserIdRef>>();

                    //let banned_users_request =
                    //    GetBannedUsersRequest::broadcaster_id(request.channel.alias_id.to_string())
                    //        .users(ids);

                    //let mut banned_users: Vec<BannedUser> = Vec::new();

                    //if let Ok(response) = instance_bundle
                    //    .twitch_api_client
                    //    .req_get(banned_users_request, &*instance_bundle.twitch_api_token)
                    //    .await
                    //{
                    //    banned_users.extend(response.data);
                    //}

                    let mut msgs: Vec<String> = Vec::new();
                    let ban_emoji = "⛔";
                    let ok_emoji = "✅";

                    for user in data {
                        let t_ban_reason = if let Some(reason) = user.ban_reason {
                            format!(": {}", reason)
                        } else {
                            "".to_string()
                        };

                        //let c_ban = if let Some(c_user) = banned_users
                        //    .iter()
                        //    .find(|x| x.user_id.clone().take().eq(&user.id))
                        //{
                        //    instance_bundle.localizator.formatted_text_by_request(
                        //       &request,
                        //        LineId::UserIdChatban,
                        //        vec![
                        //            ban_emoji.to_string(),
                        //            if let Some(reason) = c_user.reason.clone() {
                        //                format!(": {}", reason)
                        //            } else {
                        //                "".to_string()
                        //            },
                        //        ],
                        //    )
                        //} else {
                        //    "".to_string()
                        //};

                        msgs.push(instance_bundle.localizator.formatted_text_by_request(
                            &request,
                            LineId::UserIdFound,
                            vec![
                                if user.banned {
                                    ban_emoji.to_string()
                                } else {
                                    ok_emoji.to_string()
                                },
                                user.login,
                                user.id,
                                t_ban_reason,
                                // c_ban,
                            ],
                        ))
                    }

                    return Ok(Response::Multiple(msgs));
                } else {
                    return Err(ResponseError::NotFound(message));
                }
            } else {
                return Err(ResponseError::SomethingWentWrong);
            }
        }

        Err(ResponseError::NotEnoughArguments(CommandArgument::Target))
    }
}
