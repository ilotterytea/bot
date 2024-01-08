use crate::commands::*;
use std::io::Result;

use actix_web::{web, App, HttpServer};
use serde::{Deserialize, Serialize};

mod commands;

#[derive(Deserialize, Serialize)]
pub struct Response<T> {
    pub status_code: u32,
    pub message: Option<String>,
    pub data: Option<T>,
}

#[actix_web::main]
async fn main() -> Result<()> {
    let (host, port) = ("0.0.0.0", 8085);
    println!("Running the API server at {}:{}", host, port);

    let command_docs = web::Data::new(CommandDocInstance::new());

    HttpServer::new(move || {
        App::new().app_data(command_docs.clone()).service(
            web::scope("/v1").service(
                web::scope("/docs")
                    .service(web::resource("").get(get_available_docs))
                    .service(web::resource("/{name:.*}").get(get_doc)),
            ),
        )
    })
    .bind((host, port))?
    .run()
    .await
}
