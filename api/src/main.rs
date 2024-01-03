use std::io::Result;

use actix_web::{App, HttpServer};

#[actix_web::main]
async fn main() -> Result<()> {
    let (host, port) = ("127.0.0.1", 8080);
    println!("Running the API server at {}:{}", host, port);

    HttpServer::new(|| App::new())
        .bind((host, port))?
        .run()
        .await
}
