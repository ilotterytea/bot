use actix_web::{web, HttpResponse};
use chrono::{Duration, NaiveDateTime, Utc};
use common::config::Configuration;
use common::establish_connection;
use common::models::{NewSessionState, Session};
use common::{
    models::{NewSession, NewUser, SessionState, User},
    schema::{session_states::dsl as ss, sessions::dsl as se, users::dsl as us},
};
use diesel::{delete, insert_into, ExpressionMethods, QueryDsl, RunQueryDsl};
use rand::Rng;
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

#[derive(Deserialize)]
pub struct AuthenticationFail {
    pub error: String,
    pub error_description: String,
    pub state: String,
}

pub async fn authenticate(
    config: web::Data<Configuration>,
    success: Option<web::Query<AuthenticationSuccess>>,
    fail: Option<web::Query<AuthenticationFail>>,
    request: Option<web::Json<AuthenticationRequest>>,
) -> HttpResponse {
    if let Some(success) = success {
        return authenticate_success(config, success).await;
    }

    if let Some(fail) = fail {
        return authenticate_fail(fail).await;
    }

    if let Some(request) = request {
        return generate_authentication(config, request).await;
    }

    HttpResponse::BadRequest().json(Response {
        status_code: 400,
        message: None,
        data: None::<AuthenticationResponse>,
    })
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

pub async fn authenticate_fail(query: web::Query<AuthenticationFail>) -> HttpResponse {
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

    HttpResponse::BadRequest().json(Response {
        status_code: 400,
        message: Some(format!(
            "Failed to retrieve the code. Error: {}. Reason: {}",
            query.error, query.error_description
        )),
        data: None::<AuthenticationResponse>,
    })
}

#[derive(Deserialize)]
pub struct AuthenticationRequest {
    pub scopes: Vec<String>,
}

#[derive(Serialize)]
pub struct AuthenticationRequestRes {
    pub url: String,
    pub state_expires_in: u32,
}

pub async fn generate_authentication(
    config: web::Data<Configuration>,
    req: web::Json<AuthenticationRequest>,
) -> HttpResponse {
    let twitch_app_credentials = match config.credentials.twitch_app.clone() {
        Some(v) => v,
        None => {
            return HttpResponse::InternalServerError().json(Response {
                status_code: 500,
                message: Some("Secrets for Twitch API are not set on this instance".into()),
                data: None::<AuthenticationRequestRes>,
            });
        }
    };

    let conn = &mut establish_connection();
    const STATE_LENGTH: usize = 64;
    const STATE_CHAR_POOL: &[u8] = b"ABCDEFabcdef0123456789";

    let mut state = String::with_capacity(STATE_LENGTH);
    let mut rng = rand::thread_rng();

    loop {
        for _ in 0..STATE_LENGTH {
            state.push(STATE_CHAR_POOL[rng.gen::<usize>() % STATE_CHAR_POOL.len()] as char);
        }

        if let Err(_) = ss::session_states
            .find(&state)
            .get_result::<SessionState>(conn)
        {
            break;
        }

        state.clear();
    }

    insert_into(ss::session_states)
        .values([NewSessionState {
            state: state.clone(),
        }])
        .execute(conn)
        .expect("Failed to put a new session state");

    let url = format!(
        "https://id.twitch.tv/oauth2/authorize?response_type=code&redirect_uri={}&client_id={}&scope={}&state={}",
        twitch_app_credentials.redirect_uri,
        twitch_app_credentials.client_id,
        req.scopes.join("%20"),
        state,
    );

    HttpResponse::Ok().json(Response {
        status_code: 200,
        message: None,
        data: Some(AuthenticationRequestRes {
            url,
            state_expires_in: 300000,
        }),
    })
}
