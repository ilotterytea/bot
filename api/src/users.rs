use actix_web::{HttpRequest, HttpResponse};
use common::{
    establish_connection,
    models::User,
    schema::{sessions::dsl as ss, user_tokens::dsl as ut, users::dsl as us},
};
use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};
use serde::Serialize;
use uuid::Uuid;

use crate::Response;

pub async fn get_user_by_client_token(request: HttpRequest) -> HttpResponse {
    let token_from_request = if let Some(v) = request.headers().get("Authorization") {
        if let Ok(v) = v.to_str() {
            v
        } else {
            return HttpResponse::Unauthorized().json(Response {
                status_code: 401,
                message: Some("Token is not a string".to_string()),
                data: None::<User>,
            });
        }
    } else {
        return HttpResponse::Unauthorized().json(Response {
            status_code: 401,
            message: Some("No token in \"Authorization\" header.".to_string()),
            data: None::<User>,
        });
    };

    let token = if let Ok(v) = Uuid::parse_str(token_from_request) {
        v
    } else {
        return HttpResponse::Unauthorized().json(Response {
            status_code: 401,
            message: Some("Token is not a UUID-like.".to_string()),
            data: None::<User>,
        });
    };

    let conn = &mut establish_connection();
    let user_id = match ut::user_tokens
        .filter(ut::token.eq(&token))
        .select(ut::user_id)
        .get_result::<i32>(conn)
    {
        Ok(id) => id,
        Err(_) => {
            return HttpResponse::Unauthorized().json(Response {
                status_code: 401,
                message: Some("No user found with this token.".to_string()),
                data: None::<User>,
            })
        }
    };

    match us::users.find(&user_id).get_result::<User>(conn) {
        Ok(v) => HttpResponse::Ok().json(Response {
            status_code: 200,
            message: None,
            data: Some(v),
        }),
        Err(_) => HttpResponse::Unauthorized().json(Response {
            status_code: 401,
            message: Some(
                "No user found, but somehow the user ID to this token is found.".to_string(),
            ),
            data: None::<User>,
        }),
    }
}

#[derive(Serialize, Hash, PartialEq, Eq)]
#[serde(rename_all = "lowercase")]
pub enum SettingsScopeType {
    Chatters,
}

#[derive(Serialize, PartialEq, Eq, Hash)]
pub struct SettingsScope {
    pub name: SettingsScopeType,
    pub value: bool,
}

#[derive(Serialize)]
pub struct GetSettingsResponse {
    pub scopes: Vec<SettingsScope>,
}

pub async fn get_user_settings(request: HttpRequest) -> HttpResponse {
    let token_from_request = if let Some(v) = request.headers().get("Authorization") {
        if let Ok(v) = v.to_str() {
            v
        } else {
            return HttpResponse::Unauthorized().json(Response {
                status_code: 401,
                message: Some("Token is not a string".to_string()),
                data: None::<User>,
            });
        }
    } else {
        return HttpResponse::Unauthorized().json(Response {
            status_code: 401,
            message: Some("No token in \"Authorization\" header.".to_string()),
            data: None::<User>,
        });
    };

    let token = if let Ok(v) = Uuid::parse_str(token_from_request) {
        v
    } else {
        return HttpResponse::Unauthorized().json(Response {
            status_code: 401,
            message: Some("Token is not a UUID-like.".to_string()),
            data: None::<User>,
        });
    };

    let conn = &mut establish_connection();
    let user_id = match ut::user_tokens
        .filter(ut::token.eq(&token))
        .select(ut::user_id)
        .get_result::<i32>(conn)
    {
        Ok(id) => id,
        Err(_) => {
            return HttpResponse::Unauthorized().json(Response {
                status_code: 401,
                message: Some("No user found with this token.".to_string()),
                data: None::<User>,
            })
        }
    };

    let sessions: Vec<Vec<Option<String>>> = ss::sessions
        .filter(ss::user_id.eq(&user_id))
        .select(ss::scopes)
        .get_results::<Vec<Option<String>>>(conn)
        .unwrap_or_else(|_| Vec::default());

    let mut scope_vec: Vec<SettingsScope> = Vec::new();

    for session in sessions {
        for scope in session.iter().flatten() {
            let settings_scope = match scope.as_str() {
                "moderator:read:chatters" => SettingsScopeType::Chatters,
                _ => continue,
            };

            if !scope_vec.iter().any(|x| x.name.eq(&settings_scope)) {
                scope_vec.push(SettingsScope {
                    name: settings_scope,
                    value: true,
                });
            }
        }
    }

    let scope_array = [SettingsScopeType::Chatters];

    for scope in scope_array {
        if !scope_vec.iter().any(|x| x.name.eq(&scope)) {
            scope_vec.push(SettingsScope {
                name: scope,
                value: false,
            });
        }
    }

    HttpResponse::Ok().json(Response {
        status_code: 200,
        message: None,
        data: Some(GetSettingsResponse { scopes: scope_vec }),
    })
}
