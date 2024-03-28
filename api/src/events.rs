use actix_web::{web, HttpResponse};
use common::{establish_connection, models::Event, schema::events::dsl as ev};
use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};

use crate::Response;

pub async fn get_channel_events(id: web::Path<i32>) -> HttpResponse {
    let conn = &mut establish_connection();

    match ev::events
        .filter(ev::channel_id.eq(&*id))
        .get_results::<Event>(conn)
    {
        Ok(v) => HttpResponse::Ok().json(Response {
            status_code: 200,
            message: None,
            data: Some(v),
        }),
        Err(_) => HttpResponse::NotFound().json(Response {
            status_code: 404,
            message: None,
            data: None::<Vec<Event>>,
        }),
    }
}
