use actix_web::{web, HttpResponse};
use common::{establish_connection, models::Channel, schema::channels::dsl as ch};
use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};

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

pub async fn get_channels_by_alias_ids(ids: web::Path<String>) -> HttpResponse {
    let conn = &mut establish_connection();
    let ids = ids.split(',').collect::<Vec<&str>>();

    let ids = ids
        .iter()
        .filter(|x| x.parse::<i32>().is_ok())
        .map(|x| x.parse::<i32>().unwrap())
        .collect::<Vec<i32>>();

    match ch::channels
        .filter(ch::alias_id.eq_any(ids))
        .get_results::<Channel>(conn)
    {
        Ok(v) => HttpResponse::Ok().json(Response {
            status_code: 200,
            message: None,
            data: Some(v),
        }),
        Err(_) => HttpResponse::NotFound().json(Response {
            status_code: 404,
            message: None,
            data: None::<Vec<Channel>>,
        }),
    }
}

pub async fn get_channel_by_id(id: web::Path<i32>) -> HttpResponse {
    let conn = &mut establish_connection();

    match ch::channels.find(&*id).get_result::<Channel>(conn) {
        Ok(v) => HttpResponse::Ok().json(Response {
            status_code: 200,
            message: None,
            data: Some(v),
        }),
        Err(_) => HttpResponse::NotFound().json(Response {
            status_code: 404,
            message: None,
            data: None::<Channel>,
        }),
    }
}
