use serde::Deserialize;
use std::collections::HashMap;
use std::str::from_utf8;

/// Available line IDs for localization files.
#[derive(Debug, Hash, PartialEq, Eq, Deserialize)]
pub enum LineId {
    MSG_TEST,

    PROVIDERS_SEVENTV,

    EMOTES_UPDATE,
    EMOTES_PULLED,
    EMOTES_PUSHED,

    CMD_PING_RESPONSE,

    CMD_JOIN_RESPONSE,
    CMD_JOIN_RESPONSE_IN_NEW_CHAT,
    CMD_JOIN_ALREADY_JOINED,
}

/// Localization manager.
pub struct Localizations {
    pub localizations: HashMap<String, HashMap<LineId, String>>,
}

/// Localization directory.
static LOCALIZATION_DIR: include_dir::Dir = include_dir::include_dir!("localizations");

impl Localizations {
    fn get_localizations() -> HashMap<String, HashMap<LineId, String>> {
        let mut localizations: HashMap<String, HashMap<LineId, String>> = HashMap::new();
        let files = LOCALIZATION_DIR.files();

        for file in files {
            let file_name = file.path();
            let language = file_name.file_stem().and_then(|s| s.to_str()).unwrap();

            let contents = from_utf8(file.contents()).expect("Failed to read file");
            let data: HashMap<LineId, String> =
                serde_json::from_str(contents).expect("Failed to deserialize JSON");

            let map = localizations.entry(language.to_string()).or_default();

            for (line_id, line) in data {
                map.insert(line_id, line);
            }
        }

        localizations
    }

    /// Get the line by LineId and the specified language.
    pub fn literal_text(locale_id: &str, line_id: LineId) -> Option<String> {
        let localizations = Self::get_localizations();

        localizations.get(locale_id)?.get(&line_id).cloned()
    }

    /// Replace special placeholders "{}" in the line obtained via literal_text() with the provided parameters.
    /// It will replace them on the principle that the first encountered placeholder will be the first element from the parameter, and so on.
    /// This will be so until infinity, until there are no more placeholders in the text, i.e. the parameters will be repeated.
    /// For example, if there are 3 parameters and 4 placeholders, the fourth placeholder will be the first element of the parameters.
    pub fn formatted_text(
        locale_id: &str,
        line_id: LineId,
        parameters: Vec<String>,
    ) -> Option<String> {
        let binding = Self::literal_text(locale_id, line_id)?;
        let message: Vec<&str> = binding.split(' ').collect();
        let mut final_message = String::new();
        let mut index = 0;

        for word in message {
            let w = if word.contains("{}") {
                index += 1;
                word.replace("{}", parameters.get(index - 1).unwrap())
            } else {
                word.to_string()
            };

            final_message.push_str(w.as_str());
            final_message.push(' ');

            if parameters.len() == index {
                index = 0;
            }
        }

        Some(final_message)
    }
}
