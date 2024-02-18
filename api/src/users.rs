use actix_web::{HttpRequest, HttpResponse};
use common::{
    establish_connection,
    models::User,
    schema::{user_tokens::dsl as ut, users::dsl as us},
};
use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};
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
