use std::str::FromStr;

use async_trait::async_trait;
use diesel::{delete, insert_into, BelongingToDsl, ExpressionMethods, QueryDsl, RunQueryDsl};
use eyre::Result;
use twitch_api::types::NicknameRef;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command, CommandArgument,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    utils::diesel::establish_connection,
};

use common::{
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
        // TODO: Make a "subs" subcommand
        vec!["sub".to_string(), "unsub".to_string()]
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let subcommand_id = match request.subcommand_id {
            Some(v) => v,
            None => {
                return Err(ResponseError::NotEnoughArguments(
                    CommandArgument::Subcommand,
                ))
            }
        };

        if request.message.is_none() {
            return Err(ResponseError::NotEnoughArguments(CommandArgument::Target));
        }
        let message = request.message.unwrap();
        let mut message_split = message.split(" ").collect::<Vec<&str>>();

        let (target_name, event_type) = match message_split.first() {
            Some(v) => {
                let v = v.to_string();

                message_split.remove(0);

                let vec = v.split(":").collect::<Vec<&str>>();

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

        let conn = &mut establish_connection();
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

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::NotifySub,
                        vec![
                            request.sender.alias_name.clone(),
                            target_name,
                            event_type.to_string(),
                        ],
                    )
                    .unwrap()
            }
            ("sub", Some(e)) if subs.iter().any(|x| x.event_id == e.id) => instance_bundle
                .localizator
                .get_formatted_text(
                    request.channel_preference.language.as_str(),
                    LineId::NotifyAlreadySub,
                    vec![
                        request.sender.alias_name.clone(),
                        target_name,
                        event_type.to_string(),
                    ],
                )
                .unwrap(),
            ("unsub", Some(e)) if !subs.iter().any(|x| x.event_id == e.id) => instance_bundle
                .localizator
                .get_formatted_text(
                    request.channel_preference.language.as_str(),
                    LineId::NotifyAlreadyUnsub,
                    vec![
                        request.sender.alias_name.clone(),
                        target_name,
                        event_type.to_string(),
                    ],
                )
                .unwrap(),
            ("unsub", Some(e)) if subs.iter().any(|x| x.event_id == e.id) => {
                let sub = subs.iter().find(|x| x.event_id == e.id).unwrap();

                delete(evs::event_subscriptions.find(&sub.id))
                    .execute(conn)
                    .expect("Failed to delete the event subscription");

                instance_bundle
                    .localizator
                    .get_formatted_text(
                        request.channel_preference.language.as_str(),
                        LineId::NotifyUnsub,
                        vec![
                            request.sender.alias_name.clone(),
                            target_name,
                            event_type.to_string(),
                        ],
                    )
                    .unwrap()
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
