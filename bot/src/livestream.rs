use std::{sync::Arc, time::Duration};

use common::{establish_connection, models::EventType};
use log::{error, info};
use twitch_api::{
    helix::streams::GetStreamsRequest,
    types::{UserId, UserIdRef},
};

use crate::{handlers::handle_stream_event, instance_bundle::InstanceBundle};

pub struct TwitchLivestreamHelper {
    bundle: Arc<InstanceBundle>,

    online_channels_cache: Vec<UserId>,
}

impl TwitchLivestreamHelper {
    pub fn new(bundle: Arc<InstanceBundle>) -> Self {
        Self {
            bundle,
            online_channels_cache: Vec::new(),
        }
    }

    pub async fn run(&mut self) {
        info!("Starting to listen to stream events...");

        loop {
            self.process_channels().await;
            tokio::time::sleep(Duration::from_secs(5)).await;
        }
    }

    pub async fn process_channels(&mut self) {
        let data = self.bundle.twitch_livestream_websocket_data.lock().await;

        let channel_ids = data
            .listening_channel_ids
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
                    if self.online_channels_cache.contains(&stream.user_id) {
                        continue;
                    }

                    self.online_channels_cache.push(stream.user_id.clone());

                    handle_stream_event(
                        conn,
                        self.bundle.clone(),
                        stream.user_id.clone(),
                        EventType::Live,
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

                    handle_stream_event(conn, self.bundle.clone(), id, EventType::Offline).await;
                }
            }
            Err(e) => error!("Failed to get streams: {:?}", e),
        }
    }
}
