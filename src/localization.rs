use include_dir::{include_dir, Dir};
use std::{collections::HashMap, str::from_utf8};

#[derive(PartialEq, Eq, Hash, Debug)]
pub enum LineId {
    EmotesPushed,
    EmotesUpdated,
    EmotesPulled,

    Provider7TV,

    MsgError,
    MsgNoMessage,
    CommandPingResponse,
    CommandSpamNoCount,
    CommandSpamInvalidCount,
    CommandSpamResponse,
    CommandHolidayEmpty,
    CommandHolidayResponse,
    CommandJoinAlreadyJoined,
    CommandJoinResponse,
    CommandJoinResponseInChat,

    CommandTimerDeleted,
    CommandTimerEnabled,
    CommandTimerDisabled,
    CommandTimerInfo,
    CommandTimerInterval,
    CommandTimerMessage,
    CommandTimerNew,

    CommandCustomCommandDeleted,
    CommandCustomCommandEnabled,
    CommandCustomCommandDisabled,
    CommandCustomCommandInfo,
    CommandCustomCommandMessage,
    CommandCustomCommandNew,

    EventOn,
    EventOff,

    NotifySub,
    NotifyAlreadySub,
    NotifyUnsub,
    NotifyAlreadyUnsub,
    NotifySubs,
    NotifyNoSubs,

    SettingsPrefix,
    SettingsLocale,

    EventAlreadyExistsError,
    TimerAlreadyExistsError,
    CustomCommandAlreadyExistsError,
}

impl LineId {
    pub fn from_string(value: String) -> Option<Self> {
        match value.as_str() {
            "provider.7tv" => Some(Self::Provider7TV),
            "emotes.update" => Some(Self::EmotesUpdated),
            "emotes.pull" => Some(Self::EmotesPulled),
            "emotes.push" => Some(Self::EmotesPushed),
            "msg.error" => Some(Self::MsgError),
            "msg.no_message" => Some(Self::MsgNoMessage),
            "cmd.ping.response" => Some(Self::CommandPingResponse),
            "cmd.spam.no_count" => Some(Self::CommandSpamNoCount),
            "cmd.spam.invalid_count" => Some(Self::CommandSpamInvalidCount),
            "cmd.spam.response" => Some(Self::CommandSpamResponse),
            "cmd.holiday.empty" => Some(Self::CommandHolidayEmpty),
            "cmd.holiday.response" => Some(Self::CommandHolidayResponse),
            "cmd.join.already_joined" => Some(Self::CommandJoinAlreadyJoined),
            "cmd.join.response" => Some(Self::CommandJoinResponse),
            "cmd.join.response_in_chat" => Some(Self::CommandJoinResponseInChat),
            "cmd.timer.deleted" => Some(Self::CommandTimerDeleted),
            "cmd.timer.enabled" => Some(Self::CommandTimerEnabled),
            "cmd.timer.disabled" => Some(Self::CommandTimerDisabled),
            "cmd.timer.info" => Some(Self::CommandTimerInfo),
            "cmd.timer.interval" => Some(Self::CommandTimerInterval),
            "cmd.timer.message" => Some(Self::CommandTimerMessage),
            "cmd.timer.new" => Some(Self::CommandTimerNew),
            "cmd.customcommand.deleted" => Some(Self::CommandCustomCommandDeleted),
            "cmd.customcommand.enabled" => Some(Self::CommandCustomCommandEnabled),
            "cmd.customcommand.disabled" => Some(Self::CommandCustomCommandDisabled),
            "cmd.customcommand.info" => Some(Self::CommandCustomCommandInfo),
            "cmd.customcommand.message" => Some(Self::CommandCustomCommandMessage),
            "cmd.customcommand.new" => Some(Self::CommandCustomCommandNew),
            "error.customcommands.already_exists" => Some(Self::CustomCommandAlreadyExistsError),
            "event.on" => Some(Self::EventOn),
            "event.off" => Some(Self::EventOff),
            "event.already_exists" => Some(Self::EventAlreadyExistsError),
            "notify.sub" => Some(Self::NotifySub),
            "notify.already_sub" => Some(Self::NotifyAlreadySub),
            "notify.unsub" => Some(Self::NotifyUnsub),
            "notify.already_unsub" => Some(Self::NotifyAlreadyUnsub),
            "notify.subs" => Some(Self::NotifySubs),
            "notify.no_subs" => Some(Self::NotifyNoSubs),
            "settings.prefix" => Some(Self::SettingsPrefix),
            "settings.locale" => Some(Self::SettingsLocale),
            _ => None,
        }
    }
}

pub struct Localizator {
    localizations: HashMap<String, HashMap<LineId, String>>,
}

const LOCALIZATION_DIR: Dir = include_dir!("localizations");

impl Localizator {
    pub fn new() -> Self {
        let mut localizations: HashMap<String, HashMap<LineId, String>> = HashMap::new();

        for file in LOCALIZATION_DIR.files() {
            let file_name = file.path();
            let language = file_name.file_stem().and_then(|s| s.to_str()).unwrap();

            let contents = from_utf8(file.contents()).expect("Failed to read file");
            let data: HashMap<String, String> = serde_json::from_str(contents).unwrap();

            let map = localizations.entry(language.to_string()).or_default();

            for (line_id, line) in data {
                if let Some(line_id) = LineId::from_string(line_id) {
                    map.insert(line_id, line);
                }
            }
        }

        Self { localizations }
    }

    pub fn get_literal_text(&self, locale_id: &str, line_id: LineId) -> Option<String> {
        if let Some(locale) = self.localizations.get(locale_id) {
            if let Some(line) = locale.get(&line_id) {
                return Some(line.clone());
            }
        }

        None
    }

    pub fn get_formatted_text(
        &self,
        locale_id: &str,
        line_id: LineId,
        parameters: Vec<String>,
    ) -> Option<String> {
        if let Some(line) = self.get_literal_text(locale_id, line_id) {
            let new_line =
                line.split("{}")
                    .enumerate()
                    .fold(String::new(), |mut acc: String, (i, part)| {
                        acc.push_str(part);

                        if i < parameters.len() {
                            acc.push_str(parameters.get(i).unwrap());
                        }

                        acc
                    });

            return Some(new_line);
        }
        None
    }

    pub fn localization_names(&self) -> Vec<&String> {
        self.localizations.keys().collect::<Vec<&String>>()
    }
}

enum LinePlaceholder {
    SenderAliasName,
    SenderAliasId,

    TargetAliasName,
    TargetAliasId,

    RequestMessage,
    RequestSubcommandId,
    RequestCommand,

    Argument(u8),
}

impl LinePlaceholder {
    pub fn from_string(value: &String) -> Option<Self> {
        if let Ok(value) = value.parse::<u8>() {
            return Some(Self::Argument(value));
        }

        match value.as_str() {
            "sender.alias_name" => Some(Self::SenderAliasName),
            "sender.alias_id" => Some(Self::SenderAliasId),
            "target.alias_name" => Some(Self::TargetAliasName),
            "target.alias_id" => Some(Self::TargetAliasId),
            "request.message" => Some(Self::RequestMessage),
            "request.subcommand_id" => Some(Self::RequestSubcommandId),
            "request.command" => Some(Self::RequestCommand),
            _ => None,
        }
    }

    pub fn to_string(&self) -> String {
        match self {
            Self::SenderAliasName => "sender.alias_name".to_string(),
            Self::SenderAliasId => "sender.alias_id".to_string(),
            Self::TargetAliasName => "target.alias_name".to_string(),
            Self::TargetAliasId => "target.alias_id".to_string(),
            Self::RequestMessage => "request.message".to_string(),
            Self::RequestSubcommandId => "request.subcommand_id".to_string(),
            Self::RequestCommand => "request.command".to_string(),
            Self::Argument(v) => v.to_string(),
        }
    }
}
