use async_trait::async_trait;
use chrono::{Datelike, Duration, Utc};
use eyre::Result;
use rand::seq::SliceRandom;
use reqwest::StatusCode;
use substring::Substring;

use crate::{
    commands::{
        request::Request,
        response::{Response, ResponseError},
        Command,
    },
    instance_bundle::InstanceBundle,
    localization::LineId,
    shared_variables::HOLIDAY_V1_API_URL,
};

pub struct HolidayCommand;

#[async_trait]
impl Command for HolidayCommand {
    fn get_name(&self) -> String {
        "holiday".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        request: Request,
    ) -> Result<Response, ResponseError> {
        let mut today = Utc::now();

        let (month, day) = match request.message.clone() {
            Some(message) if message.eq("tommorow") => {
                today += Duration::days(1);
                (today.month(), today.day())
            }

            Some(message) if message.eq("yesterday") => {
                today -= Duration::days(1);
                (today.month(), today.day())
            }

            Some(message) if message.starts_with('.') => {
                let message = message.substring(1, message.len());

                match message.parse::<u32>() {
                    Ok(v) if (1..=12).contains(&v) => (v, today.day()),
                    _ => (today.month(), today.day()),
                }
            }

            Some(message) => {
                let message_split = message.split('.').collect::<Vec<&str>>();

                match (message_split.first(), message_split.get(1)) {
                    (Some(d), Some(m)) => match (d.parse::<u32>(), m.parse::<u32>()) {
                        (Ok(d), Ok(m)) => (m, d),
                        _ => (today.month(), today.day()),
                    },
                    (Some(d), _) => match d.parse::<u32>() {
                        Ok(d) => (today.month(), d),
                        _ => (today.month(), today.day()),
                    },
                    _ => (today.month(), today.day()),
                }
            }

            _ => (today.month(), today.day()),
        };

        let url = format!("{}/{}/{}", HOLIDAY_V1_API_URL, month, day);

        match reqwest::get(url).await {
            Ok(response) if response.status() == StatusCode::NOT_FOUND => Err(
                ResponseError::IncorrectArgument(format!("{}.{}", day, month)),
            ),
            Ok(response) => match response.json::<Vec<String>>().await {
                Ok(value) => {
                    let mut rng = rand::thread_rng();
                    let holiday = value.choose(&mut rng);

                    Ok(Response::Single(
                        instance_bundle.localizator.formatted_text_by_request(
                            &request,
                            if holiday.is_none() {
                                LineId::CommandHolidayEmpty
                            } else {
                                LineId::CommandHolidayResponse
                            },
                            if let Some(holiday) = holiday {
                                let position = value.iter().position(|x| x.eq(holiday)).unwrap();

                                vec![
                                    day.to_string(),
                                    month.to_string(),
                                    position.to_string(),
                                    value.len().to_string(),
                                    holiday.clone(),
                                ]
                            } else {
                                vec![day.to_string(), month.to_string()]
                            },
                        ),
                    ))
                }
                Err(_) => Err(ResponseError::SomethingWentWrong),
            },
            Err(e) => Err(ResponseError::ExternalAPIError(0, Some(e.to_string()))),
        }
    }
}
