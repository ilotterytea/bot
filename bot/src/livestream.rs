use std::{collections::HashMap, sync::Arc, time::Duration};

use chrono::{NaiveDateTime, Utc};
use common::{establish_connection, models::EventType};
use log::{error, info};
use twitch_api::{
    helix::{channels::GetChannelInformationRequest, streams::GetStreamsRequest},
    types::{UserId, UserIdRef},
};

use crate::{handlers::handle_stream_event, instance_bundle::InstanceBundle};

pub struct TwitchLivestreamHelper {
    bundle: Arc<InstanceBundle>,

    online_channels_cache: Vec<UserId>,
    title_channel_cache: HashMap<UserId, String>,
}

impl TwitchLivestreamHelper {
    pub fn new(bundle: Arc<InstanceBundle>) -> Self {
        Self {
            bundle,
            online_channels_cache: Vec::new(),
            title_channel_cache: HashMap::new(),
        }
    }

    pub async fn run(&mut self) {
        info!("Starting to listen to stream events...");

        loop {
            self.process_live_channels().await;
            self.process_stream_info().await;
            tokio::time::sleep(Duration::from_secs(5)).await;
        }
    }

    async fn process_live_channels(&mut self) {
        let data = self.bundle.twitch_livestream_websocket_data.lock().await;

        let channel_ids = data
            .iter()
            .map(|x| UserIdRef::from_str(x.as_str()))
            .collect::<Vec<&UserIdRef>>();

        if channel_ids.is_empty() {
            return;
        }

        let request = GetStreamsRequest::user_ids(channel_ids);

        match self
            .bundle
            .twitch_api_client
            .req_get(request, &*self.bundle.twitch_api_token)
            .await
        {
            Ok(v) => {
                let conn = &mut establish_connection();
                let streams = v.data;

                for stream in &streams {
                    // skip the stream if it started more than 2 minutes ago
                    if let Ok(stream_timestamp) = NaiveDateTime::parse_from_str(
                        stream.started_at.as_str(),
                        "%Y-%m-%dT%H:%M:%SZ",
                    ) {
                        let timestamp_now = Utc::now().naive_utc();

                        let difference_millis =
                            timestamp_now.timestamp_millis() - stream_timestamp.timestamp_millis();

                        if difference_millis > 2 * 60 * 1000 {
                            continue;
                        }
                    }

                    if self.online_channels_cache.contains(&stream.user_id) {
                        continue;
                    }

                    self.online_channels_cache.push(stream.user_id.clone());

                    handle_stream_event(
                        conn,
                        self.bundle.clone(),
                        stream.user_id.clone(),
                        EventType::Live,
                        Vec::new(),
                    )
                    .await;
                }

                let offline_channels = self
                    .online_channels_cache
                    .iter()
                    .filter(|x| !streams.iter().any(|y| y.user_id.eq(*x)))
                    .cloned()
                    .collect::<Vec<UserId>>();

                for id in offline_channels {
                    let position = self
                        .online_channels_cache
                        .iter()
                        .position(|x| x.eq(&id))
                        .unwrap();

                    self.online_channels_cache.remove(position);

                    handle_stream_event(
                        conn,
                        self.bundle.clone(),
                        id,
                        EventType::Offline,
                        Vec::new(),
                    )
                    .await;
                }
            }
            Err(e) => error!("Failed to get streams: {:?}", e),
        }
    }

    async fn process_stream_info(&mut self) {
        let data = self.bundle.twitch_livestream_websocket_data.lock().await;

        let channel_ids = data
            .iter()
            .map(|x| UserIdRef::from_str(x.as_str()))
            .collect::<Vec<&UserIdRef>>();

        if channel_ids.is_empty() {
            return;
        }

        let request = GetChannelInformationRequest::broadcaster_ids(channel_ids);

        match self
            .bundle
            .twitch_api_client
            .req_get(request, &*self.bundle.twitch_api_token)
            .await
        {
            Ok(v) => {
                let conn = &mut establish_connection();

                let channels = v.data;

                for channel in channels {
                    if let Some(old_title) = self
                        .title_channel_cache
                        .insert(channel.broadcaster_id.clone(), channel.title.clone())
                    {
                        if old_title.ne(&channel.title) {
                            handle_stream_event(
                                conn,
                                self.bundle.clone(),
                                channel.broadcaster_id.clone(),
                                EventType::Title,
                                vec![old_title, channel.title.clone()],
                            )
                            .await;
                        }
                    }
                }
            }
            Err(e) => error!("Failed to get channel infos: {:?}", e),
        }
    }
}
