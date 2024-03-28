use actix_web::{web, HttpResponse};
use common::{establish_connection, models::CustomCommand, schema::custom_commands::dsl as cc};
use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};

use crate::Response;

pub async fn get_custom_commands(id: web::Path<i32>) -> HttpResponse {
    let conn = &mut establish_connection();

    match cc::custom_commands
        .filter(cc::channel_id.eq(&*id))
        .get_results::<CustomCommand>(conn)
    {
        Ok(v) => HttpResponse::Ok().json(Response {
            status_code: 200,
            message: None,
            data: Some(v),
        }),
        Err(_) => HttpResponse::NotFound().json(Response {
            status_code: 404,
            message: None,
            data: None::<Vec<CustomCommand>>,
        }),
    }
}
