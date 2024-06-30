use actix_web::{web, HttpResponse, Responder};
use actix_web_lab::respond::Html;
use diesel::{ExpressionMethods, QueryDsl, RunQueryDsl};
use handlebars::Handlebars;
use include_dir::{include_dir, Dir};
use serde::Serialize;
use serde_json::json;
use std::env::var;

use common::{
    establish_connection,
    models::Event,
    schema::{channels::dsl as ch, event_subscriptions::dsl as evs, events::dsl as ev},
};

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
    let bot_title =
        var("WEB_BOT_TITLE").unwrap_or(var("BOT_USERNAME").unwrap_or("Some Twitch Bot".into()));

    let data = json!({
        "contact_name": contact_name,
        "contact_url": contact_url,
        "bot_title": bot_title
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
        let bot_title =
            var("WEB_BOT_TITLE").unwrap_or(var("BOT_USERNAME").unwrap_or("Some Twitch Bot".into()));

        let data = json!({
            "summary": summary,
            "content": contents,
            "contact_name": contact_name,
            "contact_url": contact_url,
            "bot_title": bot_title,
            "wiki_title": path
        });

        let body = hb.render("wiki_page.html", &data).unwrap();

        HttpResponse::Ok().body(body)
    } else {
        HttpResponse::NotFound().body("Not found")
    }
}

#[derive(Serialize)]
struct EventsForChannelHandlebars {
    pub name: String,
    pub event_type: String,
    pub message: String,
    pub flags: String,
    pub subscribers: usize,
}

pub async fn get_channel(id: web::Path<String>, hb: web::Data<Handlebars<'_>>) -> HttpResponse {
    let conn = &mut establish_connection();

    let (id, username, tid) = match ch::channels
        .filter(ch::alias_name.eq(&*id))
        .or_filter(ch::alias_id.eq((&*id).parse::<i32>().unwrap_or(-1)))
        .select((ch::id, ch::alias_name, ch::alias_id))
        .get_result::<(i32, String, i32)>(conn)
    {
        Ok(x) => x,
        Err(_) => return HttpResponse::NotFound().body("Wrong id or username"),
    };

    let mut events_hb = Vec::<EventsForChannelHandlebars>::new();
    let events: Vec<Event> = ev::events
        .filter(ev::channel_id.eq(&id))
        .get_results::<Event>(conn)
        .unwrap_or(Vec::new());

    for event in events {
        let name = match (event.target_alias_id, event.custom_alias_id) {
            (Some(x), None) => x.to_string(),
            (None, Some(x)) => x,
            _ => continue,
        };

        let subscribers = evs::event_subscriptions
            .filter(evs::event_id.eq(&event.id))
            .select(evs::event_id)
            .get_results::<i32>(conn)
            .unwrap_or(Vec::new())
            .len();

        events_hb.push(EventsForChannelHandlebars {
            name,
            event_type: event.event_type.to_string(),
            flags: if event.flags.is_empty() {
                "-".into()
            } else {
                event
                    .flags
                    .iter()
                    .map(|x| x.to_string())
                    .collect::<Vec<String>>()
                    .join(", ")
            },
            message: event.message,
            subscribers,
        })
    }

    let data = json!({
        "pfp": "",
        "username": username,
        "description": "x",
        "events": events_hb
    });

    let page = hb.render("channel.html", &data).unwrap();

    HttpResponse::Ok().body(page)
}
