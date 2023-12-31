use include_dir::{include_dir, Dir};
use std::{collections::HashMap, str::from_utf8};

use crate::commands::request::Request;

#[derive(PartialEq, Eq, Hash, Debug, Clone)]
pub enum LineId {
    ArgumentSubcommand,
    ArgumentMessage,
    ArgumentInterval,
    ArgumentName,
    ArgumentTarget,
    ArgumentValue,
    ArgumentAmount,

    MsgHint,

    HintUrlSpam,

    HintUrlMassping,
    HintUrlHoliday,
    HintUrlJoin,
    HintUrlTimer,
    HintUrlCmd,
    HintUrlEvent,
    HintUrlNotify,
    HintUrlSet,
    HintUrlEcount,
    HintUrlEtop,
    HintUrlEsim,

    MsgError,
    ErrorNotEnoughArguments,
    ErrorWrongArgumentType,
    ErrorIncorrectArgument,
    ErrorIncompatibleName,
    ErrorNamesakeCreation,
    ErrorExternalAPIError,
    ErrorNotFound,
    ErrorInsufficientRights,
    ErrorSomethingWentWrong,

    MiscDescending,
    MiscAscending,

    EmotesPushed,
    EmotesUpdated,
    EmotesPulled,

    Provider7TV,

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

    EmoteCountUsage,
    EmoteCountNotFound,

    EmoteTopResponse,
    EmoteTopNoEmotes,

    EmoteSimilaritySetSimilar,
    EmoteSimilaritySetNotSimilar,
    UserIdFound,
    UserIdChatban,

    EventAlreadyExistsError,
    TimerAlreadyExistsError,
    CustomCommandAlreadyExistsError,
}

impl LineId {
    pub fn from_string(value: String) -> Option<Self> {
        match value.as_str() {
            "argument.subcommand" => Some(Self::ArgumentSubcommand),
            "argument.message" => Some(Self::ArgumentMessage),
            "argument.interval" => Some(Self::ArgumentInterval),
            "argument.name" => Some(Self::ArgumentName),
            "argument.target" => Some(Self::ArgumentTarget),
            "argument.value" => Some(Self::ArgumentValue),
            "argument.amount" => Some(Self::ArgumentAmount),
            "msg.hint" => Some(Self::MsgHint),
            "hint.url.spam" => Some(Self::HintUrlSpam),
            "hint.url.massping" => Some(Self::HintUrlMassping),
            "hint.url.holiday" => Some(Self::HintUrlHoliday),
            "hint.url.join" => Some(Self::HintUrlJoin),
            "hint.url.timer" => Some(Self::HintUrlTimer),
            "hint.url.cmd" => Some(Self::HintUrlCmd),
            "hint.url.event" => Some(Self::HintUrlEvent),
            "hint.url.notify" => Some(Self::HintUrlNotify),
            "hint.url.set" => Some(Self::HintUrlSet),
            "hint.url.ecount" => Some(Self::HintUrlEcount),
            "hint.url.etop" => Some(Self::HintUrlEtop),
            "hint.url.esim" => Some(Self::HintUrlEsim),
            "msg.error" => Some(Self::MsgError),
            "error.not_enough_arguments" => Some(Self::ErrorNotEnoughArguments),
            "error.wrong_argument_type" => Some(Self::ErrorWrongArgumentType),
            "error.incorrect_argument" => Some(Self::ErrorIncorrectArgument),
            "error.incompatible_name" => Some(Self::ErrorIncompatibleName),
            "error.namesake_creation" => Some(Self::ErrorNamesakeCreation),
            "error.external_api_error" => Some(Self::ErrorExternalAPIError),
            "error.not_found" => Some(Self::ErrorNotFound),
            "error.insufficient_rights" => Some(Self::ErrorInsufficientRights),
            "error.something_went_wrong" => Some(Self::ErrorSomethingWentWrong),
            "misc.descending" => Some(Self::MiscDescending),
            "misc.ascending" => Some(Self::MiscAscending),
            "provider.7tv" => Some(Self::Provider7TV),
            "emotes.update" => Some(Self::EmotesUpdated),
            "emotes.pull" => Some(Self::EmotesPulled),
            "emotes.push" => Some(Self::EmotesPushed),
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
            "emote_count.usage" => Some(Self::EmoteCountUsage),
            "emote_count.not_found" => Some(Self::EmoteCountNotFound),
            "emote_top.response" => Some(Self::EmoteTopResponse),
            "emote_top.no_emotes" => Some(Self::EmoteTopNoEmotes),
            "emote_similarity.set_similar" => Some(Self::EmoteSimilaritySetSimilar),
            "emote_similarity.set_not_similar" => Some(Self::EmoteSimilaritySetNotSimilar),
            "userid.found" => Some(Self::UserIdFound),
            "userid.chat_ban" => Some(Self::UserIdChatban),
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

    pub fn get_formatted_text<P>(
        &self,
        locale_id: &str,
        line_id: LineId,
        parameters: Vec<P>,
    ) -> Option<String>
    where
        P: ToString,
    {
        Some(self.format_text_internal(locale_id, line_id, parameters, None))
    }

    pub fn formatted_text_by_request<P>(
        &self,
        request: &Request,
        line_id: LineId,
        parameters: Vec<P>,
    ) -> String
    where
        P: ToString,
    {
        self.format_text_internal(
            request.channel_preference.language.as_str(),
            line_id,
            parameters,
            Some(request),
        )
    }

    fn format_text_internal<P>(
        &self,
        locale_id: &str,
        line_id: LineId,
        parameters: Vec<P>,
        request: Option<&Request>,
    ) -> String
    where
        P: ToString,
    {
        if let Some(line) = self.get_literal_text(locale_id, line_id.clone()) {
            let placeholders = self.parse_placeholders(&line);
            let parameters = parameters
                .iter()
                .map(|x| x.to_string())
                .collect::<Vec<String>>();

            return self.replace_placeholders(line, placeholders, parameters, request);
        }

        format!(
            "🚨 No localization line for {:?} in {}!",
            line_id, locale_id
        )
    }

    fn parse_placeholders(&self, line: &String) -> Vec<LinePlaceholder> {
        let mut reading_placeholder = false;
        let mut placeholder_buffer = String::new();

        let mut placeholders: Vec<LinePlaceholder> = Vec::new();

        for c in line.chars() {
            match c {
                '{' => reading_placeholder = true,
                '}' => {
                    reading_placeholder = false;

                    if let Some(placeholder) = LinePlaceholder::from_string(&placeholder_buffer) {
                        placeholders.push(placeholder);
                    }

                    placeholder_buffer.clear();
                }
                _ => {
                    if reading_placeholder {
                        placeholder_buffer.push(c);
                    }
                }
            }
        }

        placeholders
    }

    fn replace_placeholders(
        &self,
        mut line: String,
        placeholders: Vec<LinePlaceholder>,
        parameters: Vec<String>,
        request: Option<&Request>,
    ) -> String {
        for placeholder in placeholders {
            let string = format!("{{{}}}", placeholder.to_string());

            let replacement = match (request, placeholder) {
                (_, LinePlaceholder::Argument(v)) => {
                    if let Some(value) = parameters.get(v as usize) {
                        value.clone()
                    } else {
                        string.clone()
                    }
                }
                (Some(r), LinePlaceholder::SenderAliasName) => r.sender.alias_name.clone(),
                (Some(r), LinePlaceholder::SenderAliasId) => r.sender.alias_id.to_string(),
                (Some(r), LinePlaceholder::TargetAliasName) => r.channel.alias_name.clone(),
                (Some(r), LinePlaceholder::TargetAliasId) => r.channel.alias_id.to_string(),
                (Some(r), LinePlaceholder::RequestMessage) => {
                    if let Some(message) = r.message.clone() {
                        message
                    } else {
                        "".to_string()
                    }
                }
                (Some(r), LinePlaceholder::RequestSubcommandId) => {
                    if let Some(subcommand_id) = r.subcommand_id.clone() {
                        subcommand_id
                    } else {
                        "".to_string()
                    }
                }
                (Some(r), LinePlaceholder::RequestCommand) => r.command_id.clone(),
                _ => string.clone(),
            };

            line = line.replace(string.as_str(), replacement.as_str());
        }

        line
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
