use actix_web::{web, HttpResponse};
use chrono::{Duration, NaiveDateTime, Utc};
use common::config::Configuration;
use common::establish_connection;
use common::models::Session;
use common::{
    models::{NewSession, NewUser, SessionState, User},
    schema::{session_states::dsl as ss, sessions::dsl as se, users::dsl as us},
};
use diesel::{delete, insert_into, ExpressionMethods, QueryDsl, RunQueryDsl};
use serde::{Deserialize, Serialize};
use twitch_api::twitch_oauth2::{AccessToken, UserToken};

use crate::Response;

#[derive(Serialize)]
pub struct AuthenticationResponse {
    pub token: String,
    pub expires_at: NaiveDateTime,
    pub id: i32,
    pub user: User,
}

#[derive(Deserialize)]
pub struct AuthenticationSuccess {
    pub code: String,
    pub scope: String,
    pub state: String,
}

#[derive(Deserialize, Debug)]
pub struct TwitchAccessTokenResponse {
    pub access_token: String,
    pub refresh_token: String,
    pub expires_in: i64,
    pub scope: Vec<String>,
}

pub async fn authenticate_success(
    config: web::Data<Configuration>,
    query: web::Query<AuthenticationSuccess>,
) -> HttpResponse {
    let twitch_app_credentials = match config.credentials.twitch_app.clone() {
        Some(v) => v,
        None => {
            return HttpResponse::InternalServerError().json(Response {
                status_code: 500,
                message: Some("Secrets for Twitch API are not set on this instance".into()),
                data: None::<AuthenticationResponse>,
            });
        }
    };

    let conn = &mut establish_connection();

    let q_state = query.state.clone();

    match ss::session_states
        .find(&q_state)
        .get_result::<SessionState>(conn)
    {
        Ok(v) => {
            delete(ss::session_states.find(&v.state))
                .execute(conn)
                .expect("Failed to delete state");
        }
        Err(_) => {
            return HttpResponse::BadRequest().json(Response {
                status_code: 400,
                message: Some(format!(
                    "The provided state \"{}\" isn't registered in my database.",
                    q_state
                )),
                data: None::<AuthenticationResponse>,
            })
        }
    }

    let client = reqwest::Client::new();
    let form = vec![
        ("client_id", twitch_app_credentials.client_id),
        ("client_secret", twitch_app_credentials.client_secret),
        ("code", query.code.clone()),
        ("grant_type", "authorization_code".into()),
        ("redirect_uri", twitch_app_credentials.redirect_uri),
    ];

    match client
        .post("https://id.twitch.tv/oauth2/token")
        .form(&form)
        .send()
        .await
    {
        Ok(response) => match response.json::<TwitchAccessTokenResponse>().await {
            Ok(json) => {
                let token = match UserToken::from_token(
                    &client,
                    AccessToken::from(json.access_token.clone()),
                )
                .await
                {
                    Ok(v) => v,
                    Err(_) => return HttpResponse::InternalServerError().finish(),
                };

                let expires_at = Utc::now().naive_utc() + Duration::seconds(json.expires_in);
                let alias_id = token.user_id.take().parse::<i32>().unwrap();

                let user: User = us::users
                    .filter(us::alias_id.eq(&alias_id))
                    .get_result::<User>(conn)
                    .unwrap_or_else(|_| {
                        insert_into(us::users)
                            .values([NewUser {
                                alias_id,
                                alias_name: token.login.take(),
                            }])
                            .get_result::<User>(conn)
                            .expect("Failed to insert a new user")
                    });

                let s = insert_into(se::sessions)
                    .values([NewSession {
                        access_token: json.access_token.clone(),
                        refresh_token: json.refresh_token,
                        expires_at,
                        user_id: user.id,
                        scopes: json.scope.iter().cloned().map(|x| Some(x)).collect(),
                    }])
                    .get_result::<Session>(conn)
                    .expect("Failed to insert a new session");

                HttpResponse::Ok().json(Response {
                    status_code: 200,
                    message: None,
                    data: Some(AuthenticationResponse {
                        token: json.access_token,
                        expires_at: s.expires_at,
                        id: s.id,
                        user
                    })
                })
            }
            Err(_) => HttpResponse::InternalServerError().json(Response {
                status_code: 500,
                message: Some(format!("Failed to parse Twitch API response. Reset the connection in your Twitch account settings and try again.")),
                data: None::<AuthenticationResponse>
            }),
        },
        Err(e) => HttpResponse::InternalServerError().json(Response {
            status_code: 500,
            message: Some(format!(
                "Failed to send a request to Twitch API for some reason: {}",
                e
            )),
            data: None::<AuthenticationResponse>,
        }),
    }
}
