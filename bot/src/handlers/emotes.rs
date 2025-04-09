use std::sync::Arc;

use diesel::{BelongingToDsl, ExpressionMethods, QueryDsl, RunQueryDsl};
use substring::Substring;
use twitch_emotes::emotes::Emote;

use crate::{instance_bundle::InstanceBundle, localization::LineId};
use common::{
    establish_connection,
    models::{Channel, ChannelFeature, ChannelPreference},
    schema::channels::dsl as ch,
};

pub fn handle_betterttv_emote_event(
    instance_bundle: Arc<InstanceBundle>,
    line_id: LineId,
) -> impl Fn(String, Option<String>, Emote) {
    let instances = instance_bundle.clone();

    move |channel_id, _, emote| {
        tokio::spawn({
            let instances = instances.clone();
            let channel_id = channel_id
                .substring("twitch:".len(), channel_id.len())
                .parse::<usize>()
                .expect("Error converting str to usize");
            let line_id = line_id.clone();

            async move {
                let conn = &mut establish_connection();
                let Ok(channel) = ch::channels
                    .filter(ch::alias_id.eq(channel_id as i32))
                    .get_result::<Channel>(conn)
                else {
                    return;
                };

                let Ok(channel_preference) =
                    ChannelPreference::belonging_to(&channel).get_result::<ChannelPreference>(conn)
                else {
                    return;
                };

                if channel_preference
                    .features
                    .iter()
                    .flatten()
                    .any(|x| x.eq(&ChannelFeature::SilentMode.to_string()))
                {
                    return;
                }

                instances
                    .twitch_irc_client
                    .say(
                        channel.alias_name,
                        instances.localizator.formatted_text(
                            &channel_preference.language,
                            line_id,
                            if let Some(original_code) = &emote.original_code {
                                vec!["(BTTV)", "-", &emote.code, original_code]
                            } else {
                                vec!["(BTTV)", "-", &emote.code]
                            },
                        ),
                    )
                    .await
                    .expect("Error sending message");
            }
        });
    }
}

pub fn handle_seventv_emote_event(
    instance_bundle: Arc<InstanceBundle>,
    line_id: LineId,
) -> impl Fn(String, Option<String>, Emote) {
    let instances = instance_bundle.clone();

    move |emote_set_id, actor_id, emote| {
        tokio::spawn({
            let instances = instances.clone();
            let line_id = line_id.clone();
            async move {
                let Some((channel, channel_preference, can_post)) = get_internal_data_with_seventv(
                    &instances,
                    &emote_set_id,
                    ChannelFeature::Notify7TVUpdates,
                )
                .await
                else {
                    return;
                };

                if !can_post {
                    return;
                }

                let actor_name: String = if let Some(actor_id) = &actor_id {
                    let Some(stv_user) = instances.stv_api_client.get_user_by_id(&actor_id).await
                    else {
                        return;
                    };

                    Some(stv_user.username)
                } else {
                    None
                }
                .unwrap_or("-".into());

                instances
                    .twitch_irc_client
                    .say(
                        channel.alias_name,
                        instances.localizator.formatted_text(
                            &channel_preference.language,
                            line_id,
                            if let Some(original_code) = &emote.original_code {
                                vec!["(7TV)", &actor_name, &emote.code, original_code]
                            } else {
                                vec!["(7TV)", &actor_name, &emote.code]
                            },
                        ),
                    )
                    .await
                    .expect("Error sending message");
            }
        });
    }
}

async fn get_internal_data_with_seventv(
    instances: &InstanceBundle,
    emote_set_id: &str,
    feature: ChannelFeature,
) -> Option<(Channel, ChannelPreference, bool)> {
    let emote_set = instances
        .stv_api_client
        .get_emote_set(&emote_set_id)
        .await?;

    let conn = &mut establish_connection();

    let channel = ch::channels
        .filter(ch::alias_id.eq(emote_set.owner.alias_id as i32))
        .filter(ch::opt_outed_at.is_null())
        .get_result::<Channel>(conn)
        .ok()?;

    let channel_preference = ChannelPreference::belonging_to(&channel)
        .get_result::<ChannelPreference>(conn)
        .ok()?;

    if channel_preference
        .features
        .iter()
        .flatten()
        .any(|x| x.eq(&ChannelFeature::SilentMode.to_string()))
    {
        return None;
    }

    let is_enabled = channel_preference
        .features
        .iter()
        .flatten()
        .any(|x| x.eq(&feature.to_string()));

    Some((channel, channel_preference, is_enabled))
}
