use std::str::FromStr;

use async_trait::async_trait;
use diesel::{delete, insert_into, BelongingToDsl, ExpressionMethods, QueryDsl, RunQueryDsl};
use eyre::Result;
use twitch_api::{
    helix::users::GetUsersRequest,
    types::{NicknameRef, UserId, UserIdRef},
};

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command, CommandArgument,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
};

use common::{
    establish_connection,
    models::{Event, EventSubscription, EventType, NewEventSubscription},
    schema::{event_subscriptions::dsl as evs, events::dsl as ev},
};

pub struct NotifyCommand;

#[async_trait]
impl Command for NotifyCommand {
    fn get_name(&self) -> String {
        "notify".to_string()
    }

    fn get_subcommands(&self) -> Vec<String> {
        vec![
            "sub".to_string(),
            "unsub".to_string(),
            "subs".to_string(),
            "list".to_string(),
        ]
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let subcommand_id = match request.subcommand_id.clone() {
            Some(v) => v,
            None => {
                return Err(ResponseError::NotEnoughArguments(
                    CommandArgument::Subcommand,
                ))
            }
        };

        let conn = &mut establish_connection();
        match subcommand_id.as_str() {
            "subs" => {
                let subs: Vec<EventSubscription> = EventSubscription::belonging_to(&request.sender)
                    .get_results::<EventSubscription>(conn)
                    .expect("Failed to get event subscriptions");

                let events: Vec<Event> = ev::events
                    .filter(ev::channel_id.eq(&request.channel.id))
                    .get_results::<Event>(conn)
                    .expect("Failed to get events");

                let events: Vec<&Event> = events
                    .iter()
                    .filter(|x| subs.iter().any(|y| x.id == y.event_id))
                    .collect::<Vec<&Event>>();

                if events.is_empty() {
                    return Ok(Response::Single(
                        instance_bundle.localizator.formatted_text_by_request(
                            &request,
                            LineId::NotifyNoSubs,
                            Vec::<String>::new(),
                        ),
                    ));
                }

                let target_ids: Vec<UserId> = events
                    .iter()
                    .flat_map(|x| x.target_alias_id)
                    .map(|x| UserId::new(x.to_string()))
                    .collect::<Vec<UserId>>();

                let target_ids = target_ids
                    .iter()
                    .map(|x| x.as_ref())
                    .collect::<Vec<&UserIdRef>>();

                let helix_request = GetUsersRequest::ids(target_ids.as_slice());

                let mut t_subs: Vec<String> = Vec::new();

                if let Ok(helix_response) = instance_bundle
                    .twitch_api_client
                    .req_get(helix_request, &*instance_bundle.twitch_api_token)
                    .await
                {
                    let users = helix_response.data;

                    for user in users {
                        let id = user.id.take().parse::<i32>().unwrap();
                        if let Some(e) = events
                            .iter()
                            .filter(|x| x.target_alias_id.is_some())
                            .find(|x| x.target_alias_id.unwrap() == id)
                        {
                            t_subs.push(format!(
                                "{}:{}",
                                user.login.take(),
                                e.event_type.to_string()
                            ));
                        }
                    }
                }

                for event in events.iter().filter(|x| x.custom_alias_id.is_some()) {
                    t_subs.push(format!(
                        "{}:{} *",
                        event.custom_alias_id.clone().unwrap(),
                        event.event_type.to_string(),
                    ));
                }

                return Ok(Response::Single(
                    instance_bundle.localizator.formatted_text_by_request(
                        &request,
                        LineId::NotifySubs,
                        vec![t_subs.join(", ")],
                    ),
                ));
            }
            // duplicated code bruh
            "list" => {
                let events: Vec<Event> = ev::events
                    .filter(ev::channel_id.eq(&request.channel.id))
                    .get_results::<Event>(conn)
                    .expect("Failed to get events");

                if events.is_empty() {
                    return Ok(Response::Single(
                        instance_bundle.localizator.formatted_text_by_request(
                            &request,
                            LineId::NotifyListEmpty,
                            Vec::<String>::new(),
                        ),
                    ));
                }

                let target_ids: Vec<UserId> = events
                    .iter()
                    .flat_map(|x| x.target_alias_id)
                    .map(|x| UserId::new(x.to_string()))
                    .collect::<Vec<UserId>>();

                let target_ids = target_ids
                    .iter()
                    .map(|x| x.as_ref())
                    .collect::<Vec<&UserIdRef>>();

                let helix_request = GetUsersRequest::ids(target_ids.as_slice());

                let mut t_subs: Vec<String> = Vec::new();

                if let Ok(helix_response) = instance_bundle
                    .twitch_api_client
                    .req_get(helix_request, &*instance_bundle.twitch_api_token)
                    .await
                {
                    let users = helix_response.data;

                    for user in users {
                        let id = user.id.take().parse::<i32>().unwrap();
                        if let Some(e) = events
                            .iter()
                            .filter(|x| x.target_alias_id.is_some())
                            .find(|x| x.target_alias_id.unwrap() == id)
                        {
                            t_subs.push(format!(
                                "{}:{}",
                                user.login.take(),
                                e.event_type.to_string()
                            ));
                        }
                    }
                }

                for event in events.iter().filter(|x| x.custom_alias_id.is_some()) {
                    t_subs.push(format!(
                        "{}:{} *",
                        event.custom_alias_id.clone().unwrap(),
                        event.event_type.to_string(),
                    ));
                }

                return Ok(Response::Single(
                    instance_bundle.localizator.formatted_text_by_request(
                        &request,
                        LineId::NotifyList,
                        vec![t_subs.join(", ")],
                    ),
                ));
            }
            _ => {}
        }

        if request.message.is_none() {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Target));
        }
        let message = request.message.clone().unwrap();
        let mut message_split = message.split(' ').collect::<Vec<&str>>();

        let (target_name, event_type) = match message_split.first() {
            Some(v) => {
                let v = v.to_string();

                message_split.remove(0);

                let vec = v.split(':').collect::<Vec<&str>>();

                match (vec.first(), vec.get(1)) {
                    (Some(target_name), Some(event_type))
                        if !target_name.is_empty() && !event_type.is_empty() =>
                    {
                        (
                            target_name.to_string(),
                            EventType::from_str(event_type).unwrap(),
                        )
                    }
                    _ => return Err(ResponseError::IncorrectArgument(v)),
                }
            }
            None => return Err(ResponseError::NotEnoughArguments(CommandArgument::Target)),
        };

        let target_id = match instance_bundle
            .twitch_api_client
            .get_user_from_login(
                NicknameRef::from_str(target_name.as_str()),
                &*instance_bundle.twitch_api_token,
            )
            .await
        {
            Ok(Some(v)) => v.id.take().parse::<i32>().unwrap(),
            _ => -1,
        };

        if target_id == -1 && event_type != EventType::Custom {
            return Err(ResponseError::NotFound(target_name));
        }

        let events = Event::belonging_to(&request.channel)
            .filter(ev::event_type.eq(&event_type))
            .load::<Event>(conn)
            .expect("Failed to load events");

        let event = events.iter().find(|x| {
            if x.event_type == EventType::Custom {
                x.custom_alias_id.clone().unwrap().eq(&target_name)
            } else {
                x.target_alias_id.clone().unwrap().eq(&target_id)
            }
        });

        let subs = evs::event_subscriptions
            .filter(evs::user_id.eq(&request.sender.id))
            .load::<EventSubscription>(conn)
            .expect("Failed to load event subscriptions");

        let response = match (subcommand_id.as_str(), event) {
            ("sub", Some(e)) if !subs.iter().any(|x| x.event_id == e.id) => {
                insert_into(evs::event_subscriptions)
                    .values([NewEventSubscription {
                        event_id: e.id,
                        user_id: request.sender.id,
                    }])
                    .execute(conn)
                    .expect("Failed to create a new event subscription");

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::NotifySub,
                    vec![target_name, event_type.to_string()],
                )
            }
            ("sub", Some(e)) if subs.iter().any(|x| x.event_id == e.id) => {
                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::NotifyAlreadySub,
                    vec![target_name, event_type.to_string()],
                )
            }
            ("unsub", Some(e)) if !subs.iter().any(|x| x.event_id == e.id) => {
                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::NotifyAlreadyUnsub,
                    vec![target_name, event_type.to_string()],
                )
            }
            ("unsub", Some(e)) if subs.iter().any(|x| x.event_id == e.id) => {
                let sub = subs.iter().find(|x| x.event_id == e.id).unwrap();

                delete(evs::event_subscriptions.find(&sub.id))
                    .execute(conn)
                    .expect("Failed to delete the event subscription");

                instance_bundle.localizator.formatted_text_by_request(
                    &request,
                    LineId::NotifyUnsub,
                    vec![target_name, event_type.to_string()],
                )
            }
            (_, None) => {
                return Err(ResponseError::NotFound(format!(
                    "{}:{}",
                    target_name,
                    event_type.to_string()
                )))
            }

            _ => return Err(ResponseError::SomethingWentWrong),
        };

        Ok(Response::Single(response))
    }
}
