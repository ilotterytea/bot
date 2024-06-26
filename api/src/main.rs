use crate::{
    auth::*, channels::*, commands::*, customcommands::*, events::*, join::*, routes::*, users::*,
};
use std::io::Result;

use actix_web::{web, App, HttpServer};
use dotenvy::dotenv;
use handlebars::Handlebars;
use handlebars_routes::{default_wiki_page, index, load_handlebars_templates, wiki_page};
use serde::{Deserialize, Serialize};

mod auth;
mod channels;
mod commands;
mod customcommands;
mod events;
mod handlebars_routes;
mod join;
mod routes;
mod users;

#[derive(Deserialize, Serialize)]
pub struct Response<T> {
    pub status_code: u32,
    pub message: Option<String>,
    pub data: Option<T>,
}

#[actix_web::main]
async fn main() -> Result<()> {
    dotenv().expect("Failed to load .env file");
    let (host, port) = ("0.0.0.0", 8085);
    println!("Running the API server at {}:{}", host, port);

    let command_docs = web::Data::new(CommandDocInstance::new());

    let mut handlebars = Handlebars::new();
    load_handlebars_templates(&mut handlebars);
    let handlebars_ref = web::Data::new(handlebars);

    HttpServer::new(move || {
        App::new()
            .app_data(command_docs.clone())
            .app_data(handlebars_ref.clone())
            .service(web::resource("/").get(index))
            .service(web::resource("/static/{filename:.*}").get(get_static_file))
            .service(web::resource("/wiki").get(default_wiki_page))
            .service(web::resource("/wiki/{name:.*}").get(wiki_page))
            .service(
                web::scope("/api/v1")
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
                            )
                            .service(web::resource("/join").post(join_channel)),
                    )
                    .service(
                        web::scope("/channel/{id}")
                            .service(web::resource("").get(get_channel_by_id))
                            .service(web::resource("/events").get(get_channel_events))
                            .service(web::resource("/custom-commands").get(get_custom_commands)),
                    )
                    .service(
                        web::scope("/user")
                            .service(web::resource("").get(get_user_by_client_token))
                            .service(web::resource("/settings").get(get_user_settings)),
                    ),
            )
    })
    .bind((host, port))?
    .run()
    .await
}
