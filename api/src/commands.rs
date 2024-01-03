use std::collections::HashMap;

use actix_web::{web, HttpResponse, Result};
use common::establish_connection;
use include_dir::{include_dir, Dir};
use serde::{Deserialize, Serialize};

use crate::Response;

#[derive(Deserialize, Serialize, Clone)]
pub struct CommandDocData {
    pub name_id: String,
    pub styled_name: String,
    pub short: String,
}

const COMMAND_DOCS: Dir = include_dir!("$CARGO_MANIFEST_DIR/../docs");

pub struct CommandDocInstance {
    pub data: Vec<CommandDocData>,
    pub md: HashMap<String, String>,
}

impl CommandDocInstance {
    pub fn new() -> Self {
        let mut data: Vec<CommandDocData> = Vec::new();
        let mut md: HashMap<String, String> = HashMap::new();

        for file in COMMAND_DOCS.files() {
            let file_name = file.path();
            let name = file_name.file_stem().and_then(|s| s.to_str()).unwrap();

            if let Some(content) = file.contents_utf8() {
                if let Ok(json) = serde_json::from_str::<CommandDocData>(content) {
                    data.push(json);
                } else {
                    md.insert(name.to_string(), content.to_string());
                }
            }
        }

        Self { md, data }
    }
}

// /v1/docs/commands
pub async fn get_available_docs(docs: web::Data<CommandDocInstance>) -> HttpResponse {
    HttpResponse::Ok().json(Response {
        status_code: 200,
        message: None,
        data: Some(docs.data.clone()),
    })
}

// /v1/docs/command/ping
pub async fn get_command_docs(
    name: web::Path<String>,
    docs: web::Data<CommandDocInstance>,
) -> HttpResponse {
    if let Some(md) = docs.md.get(&*name) {
        return HttpResponse::Ok().json(Response {
            status_code: 200,
            message: None,
            data: Some(md.clone()),
        });
    }

    HttpResponse::BadRequest().json(Response {
        status_code: 401,
        message: Some(format!("\"{}\" documentation not found", name)),
        data: None::<Vec<String>>,
    })
}
