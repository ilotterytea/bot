use crate::{auth::*, channels::*, commands::*};
use std::io::Result;

use actix_web::{web, App, HttpServer};
use common::config::{Configuration, BOT_CONFIGURATION_FILE};
use dotenvy::dotenv;
use serde::{Deserialize, Serialize};

mod auth;
mod channels;
mod commands;

#[derive(Deserialize, Serialize)]
pub struct Response<T> {
    pub status_code: u32,
    pub message: Option<String>,
    pub data: Option<T>,
}

#[actix_web::main]
async fn main() -> Result<()> {
    dotenv().expect("Failed to load .env");
    let (host, port) = ("0.0.0.0", 8085);
    println!("Running the API server at {}:{}", host, port);

    let command_docs = web::Data::new(CommandDocInstance::new());

    let config: Configuration = match toml::from_str(BOT_CONFIGURATION_FILE) {
        Ok(v) => v,
        Err(e) => panic!("Failed to parse TOML configuration file: {}", e),
    };

    let config_data = web::Data::new(config);

    HttpServer::new(move || {
        App::new()
            .app_data(command_docs.clone())
            .app_data(config_data.clone())
            .service(
                web::scope("/v1")
                    .service(
                        web::scope("/docs")
                            .service(web::resource("").get(get_available_docs))
                            .service(web::resource("/{name:.*}").get(get_doc)),
                    )
                    .service(
                        web::scope("/authenticate").service(web::resource("").to(authenticate)),
                    )
                    .service(
                        web::scope("/channels")
                            .service(web::resource("").get(get_channels))
                            .service(
                                web::resource("/alias_id/{name}").get(get_channels_by_alias_ids),
                            ),
                    ),
            )
    })
    .bind((host, port))?
    .run()
    .await
}
