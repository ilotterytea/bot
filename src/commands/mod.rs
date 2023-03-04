use crate::managers::command_loader::CommandLoader;
use substring::Substring;

use self::command::CommandData;

pub mod command;
pub mod permissions;

pub struct MessageCommandArguments {
    pub command_id: String,
    pub subcommand_id: Option<String>,
    pub options: Vec<String>,
    pub message: Option<String>,
    pub raw_message: String,
}

impl MessageCommandArguments {
    pub fn parse(commandloader: &CommandLoader, text: &str, prefix: &str) -> Option<Self> {
        let mut s = text.trim().split(' ').collect::<Vec<&str>>();

        let mut command_id = String::new();
        let mut command_data: Option<&CommandData> = None;

        if s.get(0).is_some() && s.get(0).unwrap().len() > prefix.len() {
            let _w = s.get(0).unwrap();
            command_id = _w.substring(prefix.len(), _w.len()).to_string();
            s.remove(0);

            let all_cmds = commandloader.get_loaded_commands();

            for cmd in all_cmds {
                if cmd.id.eq(&command_id) {
                    command_data = Some(cmd);
                    break;
                } else if cmd.aliases.contains(&command_id) {
                    command_data = Some(cmd);
                    command_id = cmd.id.clone();
                    break;
                } else {
                    continue;
                }
            }
        } else {
            return None;
        }

        let mut options: Vec<String> = Vec::new();

        for w in &s {
            if w.starts_with("--") && w.len() > 2 {
                if command_data.is_some()
                    && command_data
                        .unwrap()
                        .options
                        .contains(&w.substring(2, w.len()).to_string())
                {
                    options.push(w.substring(2, w.len()).to_string());
                }
            }
        }

        for o in &options {
            let index = &s.iter().position(|p| p.eq(&format!("--{}", o)));

            if index.is_some() {
                s.remove(index.unwrap());
            } else {
                continue;
            }
        }

        let mut subcommand_id: Option<String> = None;

        if s.get(0).is_some() {
            if command_data.is_some()
                && command_data
                    .unwrap()
                    .subcommands
                    .contains(&s.get(0).unwrap().to_string())
            {
                subcommand_id = Some(s.get(0).unwrap().to_string());
                s.remove(0);
            }
        }

        let mut message: Option<String> = None;

        if s.len() > 0 {
            message = Some(s.join(" ").to_string());
        }

        Some(Self {
            command_id,
            subcommand_id,
            options,
            message,
            raw_message: text.to_string(),
        })
    }
}
