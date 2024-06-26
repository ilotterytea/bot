use actix_web::{web, HttpResponse, Responder};
use actix_web_lab::respond::Html;
use handlebars::Handlebars;
use include_dir::{include_dir, Dir};
use serde_json::json;
use std::env::var;

use crate::CommandDocInstance;

const HANDLEBARS_TEMPLATES: Dir = include_dir!("$CARGO_MANIFEST_DIR/templates");

pub fn load_handlebars_templates(hb: &mut Handlebars<'_>) {
    for file in HANDLEBARS_TEMPLATES.files() {
        // fucking rust lsp sucks in this project somehow
        // so im just gonna type in the code blindly
        if let (Some(contents), Some(name)) = (file.contents_utf8(), file.path().file_name()) {
            hb.register_template_string(name.to_str().unwrap(), contents)
                .expect("Failed to register Handlebars template");
        }
    }
}

pub async fn index(hb: web::Data<Handlebars<'_>>) -> impl Responder {
    let contact_name: String = var("WEB_CONTACT_NAME").unwrap_or("someone".into());
    let contact_url: String = var("WEB_CONTACT_URL").unwrap_or("#".into());

    let data = json!({
        "contact_name": contact_name,
        "contact_url": contact_url
    });

    let body = hb.render("index.html", &data).unwrap();

    Html(body)
}

pub async fn wiki_page(
    wiki: web::Data<CommandDocInstance>,
    path: web::Path<String>,
    hb: web::Data<Handlebars<'_>>,
) -> HttpResponse {
    get_wiki_page(wiki, path.into_inner(), hb).await
}

pub async fn default_wiki_page(
    wiki: web::Data<CommandDocInstance>,
    hb: web::Data<Handlebars<'_>>,
) -> HttpResponse {
    get_wiki_page(wiki, "README".into(), hb).await
}

async fn get_wiki_page(
    wiki: web::Data<CommandDocInstance>,
    path: String,
    hb: web::Data<Handlebars<'_>>,
) -> HttpResponse {
    if let (Some(summary), Some(contents)) = (wiki.data.get("summary"), wiki.data.get(&path)) {
        let summary = markdown::to_html(summary);
        let contents = markdown::to_html(contents);

        let contact_name: String = var("WEB_CONTACT_NAME").unwrap_or("someone".into());
        let contact_url: String = var("WEB_CONTACT_URL").unwrap_or("#".into());

        let data = json!({
            "summary": summary,
            "content": contents,
            "contact_name": contact_name,
            "contact_url": contact_url
        });

        let body = hb.render("wiki_page.html", &data).unwrap();

        HttpResponse::Ok().body(body)
    } else {
        HttpResponse::NotFound().body("Not found")
    }
}
