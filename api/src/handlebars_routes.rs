use actix_web::{HttpResponse, Responder, web};
use chrono::{NaiveDateTime, Utc};
use diesel::{
    ExpressionMethods, JoinOnDsl, PgArrayExpressionMethods, QueryDsl, RunQueryDsl, dsl::not,
};
use handlebars::Handlebars;
use include_dir::{Dir, include_dir};
use serde::{Deserialize, Serialize};
use serde_json::json;
use std::collections::HashMap;
use twitch_api::{
    HelixClient,
    helix::users::{GetUsersRequest, User},
    twitch_oauth2::UserToken,
    types::UserIdRef,
};

use common::{
    config::Configuration,
    establish_connection, format_timestamp,
    models::{Channel, ChannelFeature, Event, Timer},
    schema::{
        channel_preferences::dsl as chp, channels::dsl as ch, custom_commands::dsl as cc,
        event_subscriptions::dsl as evs, events::dsl as ev, timers::dsl as ti,
    },
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

pub async fn index(
    config: web::Data<Configuration>,
    hb: web::Data<Handlebars<'_>>,
) -> impl Responder {
    let data = json!({
        "contact_name": &config.web.contact_name,
        "contact_url": &config.web.contact_url,
        "bot_title": &config.web.bot_title
    });

    let body = hb.render("index.html", &data).unwrap();

    web::Html::new(body)
}

pub async fn wiki_page(
    config: web::Data<Configuration>,
    wiki: web::Data<CommandDocInstance>,
    path: web::Path<String>,
    hb: web::Data<Handlebars<'_>>,
) -> HttpResponse {
    get_wiki_page(config, wiki, path.into_inner(), hb).await
}

pub async fn default_wiki_page(
    config: web::Data<Configuration>,
    wiki: web::Data<CommandDocInstance>,
    hb: web::Data<Handlebars<'_>>,
) -> HttpResponse {
    get_wiki_page(config, wiki, "README".into(), hb).await
}

async fn get_wiki_page(
    config: web::Data<Configuration>,
    wiki: web::Data<CommandDocInstance>,
    path: String,
    hb: web::Data<Handlebars<'_>>,
) -> HttpResponse {
    if let (Some(summary), Some(contents)) = (wiki.data.get("summary"), wiki.data.get(&path)) {
        let summary = markdown::to_html(summary);
        let contents = markdown::to_html(contents);

        let data = json!({
            "summary": summary,
            "content": contents,
            "contact_name": &config.web.contact_name,
            "contact_url": &config.web.contact_url,
            "bot_title": &config.web.bot_title,
            "wiki_title": path
        });

        let body = hb.render("wiki_page.html", &data).unwrap();

        HttpResponse::Ok()
            .content_type(mime_guess::mime::TEXT_HTML)
            .body(body)
    } else {
        HttpResponse::NotFound()
            .content_type(mime_guess::mime::TEXT_HTML)
            .body("Not found")
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

#[derive(Serialize)]
struct CustomCommandForChannelHandlebars {
    pub name: String,
    pub messages: Vec<String>,
}

#[derive(Serialize)]
struct TimerForChannelHandlebars {
    pub name: String,
    pub messages: Vec<String>,
    pub last_executed: String,
    pub interval: String,
}

pub async fn get_channel(
    config: web::Data<Configuration>,
    id: web::Path<String>,
    hb: web::Data<Handlebars<'_>>,
    hc: web::Data<HelixClient<'static, reqwest::Client>>,
    ut: web::Data<UserToken>,
) -> HttpResponse {
    let conn = &mut establish_connection();

    let internal_channel: Channel = match ch::channels
        .filter(ch::alias_name.eq(&*id))
        .or_filter(ch::alias_id.eq((&*id).parse::<i32>().unwrap_or(-1)))
        .get_result::<Channel>(conn)
    {
        Ok(x) => x,
        Err(_) => return HttpResponse::NotFound().body("Wrong id or username"),
    };

    let tid = internal_channel.alias_id.to_string();
    let request = GetUsersRequest::ids(vec![UserIdRef::from_str(tid.as_str())]);
    let channel = hc.req_get(request, &**ut).await.unwrap().data;

    if channel.is_empty() {
        return HttpResponse::NotFound().body("ID not exists in Twitch database");
    }

    let channel: &User = channel.first().unwrap();

    let mut events_hb = Vec::<EventsForChannelHandlebars>::new();
    let events: Vec<Event> = ev::events
        .filter(ev::channel_id.eq(&internal_channel.id))
        .get_results::<Event>(conn)
        .unwrap_or(Vec::new());

    // Caching usernames
    let mut user_map: HashMap<i32, String> = HashMap::new();
    let user_ids_str: Vec<String> = events
        .iter()
        .filter(|x| x.target_alias_id.is_some())
        .map(|x| {
            let id = x.target_alias_id.unwrap();
            id.to_string()
        })
        .collect();
    let user_ids: Vec<&UserIdRef> = user_ids_str
        .iter()
        .map(|x| UserIdRef::from_str(x.as_str()))
        .collect();

    let request = GetUsersRequest::ids(user_ids);
    let channels = hc.req_get(request, &**ut).await.unwrap().data;

    for channel in channels {
        user_map.insert(
            channel.id.take().parse::<i32>().unwrap(),
            channel.login.take(),
        );
    }

    for event in events {
        let name = match (event.target_alias_id, event.custom_alias_id) {
            (Some(x), None) => {
                if let Some(x) = user_map.get(&x) {
                    x.clone()
                } else {
                    x.to_string()
                }
            }
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

    let commands: Vec<(String, Vec<String>)> = cc::custom_commands
        .filter(cc::channel_id.eq(&internal_channel.id))
        .select((cc::name, cc::messages))
        .get_results::<(String, Vec<String>)>(conn)
        .unwrap_or(Vec::new());

    let commands: Vec<CustomCommandForChannelHandlebars> = commands
        .iter()
        .map(|(x, y)| CustomCommandForChannelHandlebars {
            name: x.clone(),
            messages: y.clone(),
        })
        .collect();

    let timers: Vec<Timer> = ti::timers
        .filter(ti::channel_id.eq(&internal_channel.id))
        .get_results::<Timer>(conn)
        .unwrap_or(Vec::new());
    let now = Utc::now().naive_utc();
    let timers: Vec<TimerForChannelHandlebars> = timers
        .iter()
        .map(|x| TimerForChannelHandlebars {
            name: x.name.clone(),
            messages: x.messages.clone(),
            last_executed: format_timestamp(
                (now.and_utc().timestamp() - x.last_executed_at.and_utc().timestamp()) as u64,
            ),
            interval: format_timestamp(x.interval_sec as u64),
        })
        .collect();

    let data = json!({
        "pfp": channel.profile_image_url,
        "username": internal_channel.alias_name,
        "description": channel.description,
        "events": events_hb,
        "commands": commands,
        "timers": timers,
        "contact_name": &config.web.contact_name,
        "contact_url": &config.web.contact_url,
        "bot_title": &config.web.bot_title,
        "joined": format_timestamp((Utc::now().naive_utc().and_utc().timestamp() - internal_channel.joined_at.and_utc().timestamp()) as u64),
        "opted_out": internal_channel.opt_outed_at.is_some()
    });

    let page = hb.render("channel.html", &data).unwrap();

    HttpResponse::Ok().body(page)
}

#[derive(Deserialize)]
pub struct SearchQuery {
    pub query: String,
}

#[derive(Serialize)]
struct ChannelForSearchHandlebars {
    pub id: String,
    pub image_url: String,
    pub username: String,
}

pub async fn search(
    config: web::Data<Configuration>,
    query: web::Query<SearchQuery>,
    hb: web::Data<Handlebars<'_>>,
    hc: web::Data<HelixClient<'static, reqwest::Client>>,
    ut: web::Data<UserToken>,
) -> HttpResponse {
    let conn = &mut establish_connection();

    let is_number = query.query.chars().all(char::is_numeric);
    let number_id = if is_number {
        query.query.parse::<i32>().unwrap()
    } else {
        -1
    };

    let channels: Vec<Channel> = ch::channels
        .get_results::<Channel>(conn)
        .unwrap_or(Vec::new());

    let channels: Vec<Channel> = channels
        .iter()
        .filter(|&x| {
            if is_number && number_id >= 0 {
                x.alias_id.eq(&number_id)
            } else {
                x.alias_name.eq(&query.query)
            }
        })
        .cloned()
        .collect();

    let channel_ids_str: Vec<String> = channels.iter().map(|x| x.alias_id.to_string()).collect();
    let channel_ids: Vec<&UserIdRef> = channel_ids_str
        .iter()
        .map(|x| UserIdRef::from_str(x.as_str()))
        .collect();

    let request = GetUsersRequest::ids(channel_ids.as_slice());
    let channels: Vec<ChannelForSearchHandlebars> = if !channels.is_empty() {
        hc.req_get(request, &**ut)
            .await
            .unwrap()
            .data
            .iter()
            .map(|x| ChannelForSearchHandlebars {
                username: x.login.clone().take(),
                image_url: x
                    .profile_image_url
                    .clone()
                    .unwrap_or("/static/pfp.png".into()),
                id: x.id.clone().take(),
            })
            .collect()
    } else {
        Vec::new()
    };

    let results_count = channels.len();

    let data = json!({
        "query": query.query,
        "results_found": results_count != 0,
        "results_count": results_count,
        "channels_found": !channels.is_empty(),
        "channels_count": channels.len(),
        "channels": channels,
        "contact_name": &config.web.contact_name,
        "contact_url": &config.web.contact_url,
        "bot_title": &config.web.bot_title
    });
    let body = hb.render("search.html", &data).unwrap();

    HttpResponse::Ok()
        .content_type(mime_guess::mime::TEXT_HTML)
        .body(body)
}

#[derive(Serialize, Clone)]
struct ChannelForChannelCatalogHandlebars {
    pub id: String,
    pub username: String,
    pub image_url: String,
    pub opted_out: bool,
}

pub async fn channel_catalog(
    config: web::Data<Configuration>,
    hb: web::Data<Handlebars<'_>>,
    hc: web::Data<HelixClient<'static, reqwest::Client>>,
    ut: web::Data<UserToken>,
) -> HttpResponse {
    let conn = &mut establish_connection();
    let channels: Vec<(i32, Option<NaiveDateTime>)> = ch::channels
        .inner_join(chp::channel_preferences.on(chp::channel_id.eq(ch::id)))
        .filter(not(
            chp::features.contains(vec![ChannelFeature::SilentMode.to_string()])
        ))
        .select((ch::alias_id, ch::opt_outed_at))
        .get_results::<(i32, Option<NaiveDateTime>)>(conn)
        .unwrap_or(Vec::new());

    let channel_ids_str: Vec<String> = channels.iter().map(|(x, _)| x.to_string()).collect();
    let channel_ids: Vec<&UserIdRef> = channel_ids_str
        .iter()
        .map(|x| UserIdRef::from_str(x.as_str()))
        .collect();

    let request = GetUsersRequest::ids(channel_ids.as_slice());
    let mut channels: Vec<ChannelForChannelCatalogHandlebars> = if !channels.is_empty() {
        hc.req_get(request, &**ut)
            .await
            .unwrap()
            .data
            .iter()
            .map(|x| {
                let id = x.id.clone().take().parse::<i32>().unwrap();
                ChannelForChannelCatalogHandlebars {
                    username: x.login.clone().take(),
                    image_url: x
                        .profile_image_url
                        .clone()
                        .unwrap_or("/static/pfp.png".into()),
                    id: x.id.clone().take(),
                    opted_out: channels.iter().any(|(y, z)| y.eq(&id) && z.is_some()),
                }
            })
            .collect()
    } else {
        Vec::new()
    };

    channels.sort_by_key(|x| x.opted_out);
    channels.retain(|x| x.username.ne(&config.bot.username));

    let data = json!({
        "joined_channels_count": channels.iter().filter(|x| !x.opted_out).collect::<Vec<&ChannelForChannelCatalogHandlebars>>().len(),
        "channels": channels,
        "contact_name": &config.web.contact_name,
        "contact_url": &config.web.contact_url,
        "bot_title": &config.web.bot_title
    });
    let body = hb.render("channel_catalog.html", &data).unwrap();

    HttpResponse::Ok()
        .content_type(mime_guess::mime::TEXT_HTML)
        .body(body)
}
