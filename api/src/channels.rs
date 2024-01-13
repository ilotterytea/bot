use actix_web::HttpResponse;
use common::{establish_connection, models::Channel, schema::channels::dsl as ch};
use diesel::RunQueryDsl;

use crate::Response;

pub async fn get_channels() -> HttpResponse {
    let conn = &mut establish_connection();

    let channels: Vec<Channel> = ch::channels
        .get_results(conn)
        .expect("Failed to get channels");

    HttpResponse::Ok().json(Response {
        status_code: 200,
        message: None,
        data: Some(channels),
    })
}
