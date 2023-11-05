use async_trait::async_trait;
use diesel::{insert_into, ExpressionMethods, QueryDsl, RunQueryDsl};
use twitch_irc::message::PrivmsgMessage;

use crate::{
    command::Command,
    instance_bundle::InstanceBundle,
    localization::LineId,
    message::ParsedPrivmsgMessage,
    models::diesel::{Channel, ChannelPreference, NewChannel, NewChannelPreference, User},
    schema::{channel_preferences::dsl as chp, channels::dsl as ch},
    utils::diesel::establish_connection,
};

pub struct JoinCommand;

#[async_trait]
impl Command for JoinCommand {
    fn get_name(&self) -> String {
        "join".to_string()
    }

    async fn execute(
        &self,
        instance_bundle: &InstanceBundle,
        data_message: PrivmsgMessage,
        _message: ParsedPrivmsgMessage,
        _channel: &Channel,
        channel_preferences: &ChannelPreference,
        user: &User,
    ) -> Option<Vec<String>> {
        let conn = &mut establish_connection();

        let channel_query = ch::channels
            .filter(ch::alias_id.eq(&user.alias_id))
            .load::<Channel>(conn)
            .expect("Failed to get users");

        if !channel_query.is_empty() {
            return Some(vec![instance_bundle
                .localizator
                .get_formatted_text(
                    channel_preferences.language.clone().unwrap().as_str(),
                    LineId::CommandJoinResponse,
                    vec![data_message.sender.name.clone()],
                )
                .unwrap()]);
        }

        insert_into(ch::channels)
            .values([NewChannel {
                alias_id: user.alias_id,
            }])
            .execute(conn)
            .expect("Failed to insert a new channel");

        let new_channel = ch::channels
            .filter(ch::alias_id.eq(&user.alias_id))
            .first::<Channel>(conn)
            .expect("Failed to get users");

        insert_into(chp::channel_preferences)
            .values([NewChannelPreference {
                channel_id: new_channel.id,
            }])
            .execute(conn)
            .expect("Failed to insert preferences for a new channel");

        instance_bundle
            .twitch_irc_client
            .join(data_message.sender.name.clone())
            .expect("Failed to join chat room");

        instance_bundle
            .twitch_irc_client
            .say(
                data_message.sender.name.clone(),
                instance_bundle
                    .localizator
                    .get_formatted_text(
                        channel_preferences.language.clone().unwrap().as_str(),
                        LineId::CommandJoinResponseInChat,
                        vec![data_message.sender.name.clone()],
                    )
                    .unwrap(),
            )
            .await
            .expect("Failed to send a message");

        Some(vec![instance_bundle
            .localizator
            .get_formatted_text(
                channel_preferences.language.clone().unwrap().as_str(),
                LineId::CommandJoinResponse,
                vec![data_message.sender.name.clone()],
            )
            .unwrap()])
    }
}
