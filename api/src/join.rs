use actix_web::{web, HttpRequest, HttpResponse};
use chrono::NaiveDateTime;
use common::{
    config::Configuration,
    establish_connection,
    models::{Channel, NewChannel, Session, User, UserToken as ClientToken},
    schema::{channels::dsl as ch, sessions::dsl as se, user_tokens::dsl as ut, users::dsl as us},
};
use diesel::{
    insert_into, update, ExpressionMethods, PgArrayExpressionMethods, QueryDsl, RunQueryDsl,
};
use reqwest::Client;
use serde::Deserialize;

use crate::Response;

#[derive(Deserialize)]
struct ModeratedChannelResponse {
    pub data: Vec<ModeratedChannel>,
}

#[derive(Deserialize)]
struct ModeratedChannel {
    pub broadcaster_id: String,
    pub broadcaster_login: String,
}

#[derive(Deserialize)]
pub struct JoinRequest {
    pub alias_id: i32,
}

pub async fn join_channel(
    config: web::Data<Configuration>,
    body: web::Json<JoinRequest>,
    request: HttpRequest,
) -> HttpResponse {
    let headers = request.headers();

    let auth_token = match headers.get("Authorization") {
        Some(v) => v,
        None => {
            return HttpResponse::Unauthorized().json(Response {
                status_code: 401,
                message: Some("Please provide an authorization key.".to_string()),
                data: None::<Channel>,
            })
        }
    };

    let auth_token = match auth_token.to_str() {
        Ok(v) => v,
        Err(_) => {
            return HttpResponse::BadRequest().json(Response {
                status_code: 400,
                message: Some(
                    "The provided authorization key does not contain visible ASCII characters."
                        .to_string(),
                ),
                data: None::<Channel>,
            })
        }
    };

    let auth_token = match uuid::Uuid::parse_str(auth_token) {
        Ok(v) => v,
        Err(_) => {
            return HttpResponse::BadRequest().json(Response {
                status_code: 400,
                message: Some(
                    "The provided authorization key cannot be parsed by UUID.".to_string(),
                ),
                data: None::<Channel>,
            })
        }
    };

    let conn = &mut establish_connection();

    let client_token: ClientToken = match ut::user_tokens
        .filter(ut::token.eq(auth_token))
        .get_result::<ClientToken>(conn)
    {
        Ok(v) => v,
        Err(_) => {
            return HttpResponse::Unauthorized().json(Response {
                status_code: 401,
                message: Some(format!(
                    "The provided authorization key (\"{}\") does not exist or has expired.",
                    auth_token.as_simple().to_string()
                )),
                data: None::<Channel>,
            })
        }
    };

    let user: User = match us::users
        .find(&client_token.user_id)
        .get_result::<User>(conn)
    {
        Ok(v) => v,
        Err(_) => {
            return HttpResponse::Unauthorized().json(Response {
                status_code: 401,
                message: Some(format!(
                    "There is no user with the provided authorization token \"{}\".",
                    auth_token.as_simple().to_string()
                )),
                data: None::<Channel>,
            })
        }
    };

    let (login, id): (String, i32);

    if user.alias_id.ne(&body.alias_id) {
        let user_session: Session = match se::sessions
            .filter(se::user_id.eq(&user.id))
            .filter(se::scopes.contains(vec!["user:read:moderated_channels"]))
            .get_result::<Session>(conn)
        {
            Ok(v) => v,
            Err(_) => {
                return HttpResponse::Unauthorized().json(Response {
                    status_code: 401,
                    message: Some("You are unauthorized with a Twitch account with \"user:read:moderated_channels\" scope.".to_string()),
                    data: None::<Channel>,
                })
            }
        };

        let reqwest_client = Client::default();

        let b_id = body.alias_id.to_string();

        let client_id = match config.credentials.twitch_app.clone() {
            Some(v) => v.client_id,
            None => return HttpResponse::InternalServerError().json(Response {
                status_code: 500,
                message: Some("The required parameters were not set in the server settings. Contact the owner of this instance.".to_string()),
                data: None::<Channel>
            }),
        };

        // since HelixClient doesn't support the GetModeratedChannels endpoint,
        // i have to write my own implementation
        // (maybe i'll make a PR to twitch_api crate someday........)
        match reqwest_client
            .get(format!(
                "https://api.twitch.tv/helix/moderation/channels?user_id={}",
                user.alias_id
            ))
            .header(
                "Authorization",
                format!("Bearer {}", user_session.access_token),
            )
            .header("Client-Id", client_id)
            .send()
            .await
        {
            Ok(response) => match response.json::<ModeratedChannelResponse>().await {
                Ok(json) => match json.data.iter().find(|x| x.broadcaster_id.eq(&b_id)) {
                    Some(channel) => {
                        login = channel.broadcaster_login.clone();
                        id = channel.broadcaster_id.parse::<i32>().unwrap();
                    }
                    None => {
                        return HttpResponse::Unauthorized().json(Response {
                            status_code: 401,
                            message: Some(format!("You are not a moderator of alias ID {}.", b_id)),
                            data: None::<Channel>,
                        })
                    }
                },
                Err(_) => {
                    return HttpResponse::InternalServerError().json(Response {
                        status_code: 500,
                        message: Some("Failed to parse Twitch response.".to_string()),
                        data: None::<Channel>,
                    })
                }
            },
            Err(_) => {
                return HttpResponse::InternalServerError().json(Response {
                    status_code: 500,
                    message: Some("Failed to send a request to Twitch API.".to_string()),
                    data: None::<Channel>,
                })
            }
        }
    } else {
        login = user.alias_name.clone();
        id = user.alias_id;
    }

    match ch::channels
        .filter(ch::alias_id.eq(&id))
        .get_result::<Channel>(conn)
    {
        Ok(mut v) => {
            if v.opt_outed_at.is_some() {
                match update(ch::channels.find(&v.id))
                    .set(ch::opt_outed_at.eq(None::<NaiveDateTime>))
                    .execute(conn)
                {
                    Ok(_) => {
                        v.opt_outed_at = None;

                        return HttpResponse::Ok().json(Response {
                            status_code: 200,
                            message: Some(
                                "The bot will be rejoining this channel soon!".to_string(),
                            ),
                            data: Some(v),
                        });
                    }
                    Err(_) => {
                        return HttpResponse::InternalServerError().json(Response {
                            status_code: 500,
                            message: Some("Failed to remove the 'opt_outed_at' field.".to_string()),
                            data: Some(v),
                        })
                    }
                }
            }

            HttpResponse::Conflict().json(Response {
                status_code: 409,
                message: Some("Already joined this channel!".to_string()),
                data: Some(v),
            })
        }
        Err(_) => {
            match insert_into(ch::channels)
                .values([NewChannel {
                    alias_id: id,
                    alias_name: login.clone(),
                }])
                .get_result::<Channel>(conn)
            {
                Ok(v) => HttpResponse::Ok().json(Response {
                    status_code: 200,
                    message: Some(
                        "Success! The bot will be joining this channel soon.".to_string(),
                    ),
                    data: Some(v),
                }),
                Err(_) => HttpResponse::InternalServerError().json(Response {
                    status_code: 500,
                    message: Some(format!(
                        "Failed to create a channel for alias ID {} ({})",
                        id, login
                    )),
                    data: None::<Channel>,
                }),
            }
        }
    }
}
