use std::collections::HashMap;

use actix_web::{web, HttpResponse};
use include_dir::{include_dir, Dir, DirEntry};
use serde::Serialize;

use crate::Response;

#[derive(Serialize)]
pub struct CommandDoc {
    pub name: String,
    pub content: String,
}

const COMMAND_DOCS: Dir = include_dir!("$CARGO_MANIFEST_DIR/../docs");

pub struct CommandDocInstance {
    pub data: HashMap<String, String>,
}

impl CommandDocInstance {
    pub fn new() -> Self {
        let mut data: HashMap<String, String> = HashMap::new();

        for entry in COMMAND_DOCS.entries() {
            handle_entry(entry, &mut data);
        }

        Self { data }
    }
}

fn handle_entry(entry: &DirEntry<'_>, data: &mut HashMap<String, String>) {
    if let Some(dir) = entry.as_dir() {
        handle_dir(dir.entries(), data);
    }

    if let Some(file) = entry.as_file() {
        if let Some(path) = file.path().to_str() {
            if let Some(contents) = file.contents_utf8() {
                let path = if path.ends_with(".md") {
                    path[..path.len() - 3].to_string()
                } else {
                    path.to_string()
                };

                data.insert(path, contents.to_string());
            }
        }
    }
}

fn handle_dir(entries: &[DirEntry<'_>], data: &mut HashMap<String, String>) {
    for entry in entries {
        handle_entry(entry, data);
    }
}

pub async fn get_available_docs(data: web::Data<CommandDocInstance>) -> HttpResponse {
    HttpResponse::Ok().json(Response {
        status_code: 200,
        message: None,
        data: Some(data.data.keys().cloned().collect::<Vec<String>>()),
    })
}

pub async fn get_doc(path: web::Path<String>, data: web::Data<CommandDocInstance>) -> HttpResponse {
    if let Some(entry) = data.data.get(&*path) {
        return HttpResponse::Ok().json(Response {
            status_code: 200,
            message: None,
            data: Some(CommandDoc {
                name: path.clone(),
                content: entry.clone(),
            }),
        });
    }

    HttpResponse::NotFound().json(Response {
        status_code: 404,
        message: Some(format!("Documentation \"{}\" does not exist!", path)),
        data: None::<CommandDoc>,
    })
}
