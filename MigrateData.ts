// Copyright (C) 2022 NotDankEnough (ilotterytea)
// 
// This file is part of itb2.
// 
// itb2 is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// itb2 is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with itb2.  If not, see <http://www.gnu.org/licenses/>.

import EmoteLib from "emotelib";
import IEmote from "emotelib/dist/interfaces/IEmote";
import { readFileSync, writeFileSync } from "fs";
import { Logger } from "tslog";
import TwitchApi from "./apollo/clients/ApiClient";
import ConfigIni from "./apollo/files/ConfigIni";
import IConfiguration from "./apollo/interfaces/IConfiguration";
import IStorage from "./apollo/interfaces/IStorage";

const log: Logger = new Logger({name: "migrator"});

namespace Migrator {
    export async function convertTargetDataToV2() {
    }

    export async function convertToV2Data(file_path: string, new_file_path: string) {
        try {
            const cfg: IConfiguration = await ConfigIni.parse("config.ini");
            const elib: EmoteLib = new EmoteLib({client_id: cfg.Authorization.ClientID, access_token: cfg.Authorization.AccessToken});
            const tapi: TwitchApi.Client = new TwitchApi.Client(cfg.Authorization.ClientID, cfg.Authorization.AccessToken);

            const old_data: IStorage.V1Main = JSON.parse(readFileSync(file_path, {
                encoding: "utf-8"
            }));

            if (old_data.version !== "v1") {
                log.error("Provided file", file_path, "isn't version 1. Shutting down...");
                process.exit(1);
            }
    
            const data: IStorage.Main = {
                Version: "v2",
                Join: {
                    AsClient: [],
                    AsAnonymous: []
                },
                Global: {
                    Prefix: "",
                    Modules: {},
                    Users: {}
                }
            };
            
            // Setting the prefix:
            data.Global.Prefix = old_data.prefix;

            // Converting the string ID to number ID and adding to array:
            // // For clients:
            old_data.join.asclient.forEach((id) => {
                if (isNaN(parseInt(id))) {
                    log.warn("ID", id, "is not a number.");
                    return;
                }

                data.Join.AsClient.push(parseInt(id));
            });
            // // For anonymous:
            old_data.join.asanonymous.forEach((id) => {
                if (isNaN(parseInt(id))) {
                    log.warn("ID", id, "is not a number.");
                    return;
                }
                
                data.Join.AsAnonymous.push(parseInt(id));
            });

            // Creating users:
            Object.keys(old_data.roles).forEach((role) => {
                old_data.roles[role].forEach((id) => {
                    data.Global.Users[id] = {
                        InternalType: role
                    }
                });
            });

            // Creating targets:
            Object.keys(old_data.emotes).forEach(async (channel) => {
                const t_channel = await tapi.getUserByName(channel);

                if (t_channel === undefined) return false;
                
                var emotes: IEmote.STV[] | null = await elib.seventv.getChannelEmotes(channel);

                var target: IStorage.Target = {
                    Emotes: {},
                    Modules: {},
                    SuccessfullyCompletedTests: old_data.stats.tests[channel],
                    ExecutedCommands: old_data.stats.executed_commands[channel],
                    ChatLines: old_data.stats.chat_lines[channel],
                    Name: channel
                }

                if (emotes !== null) {
                    var old_emotes = old_data.emotes[channel];
                    target.Emotes!["stv"] = {};
                    var unknown_emotes: {[id: string]: number} = {};

                    emotes.forEach((emote) => {
                        Object.keys(old_emotes).forEach((_emote) => {
                            if (_emote == emote.name) {
                                target.Emotes!["stv"][_emote] = {
                                    ID: emote.id,
                                    UsedTimes: old_emotes[_emote]
                                }
                            } else {
                                unknown_emotes[_emote] = old_emotes[_emote];
                            }
                        });
                    });

                    log.debug(unknown_emotes, channel);
                }

                writeFileSync(t_channel?.id + ".json",
                    JSON.stringify(target, null, 2), {encoding: "utf-8"}
                );
            });

            log.debug("Saving the global file in V2...");
            writeFileSync(new_file_path, JSON.stringify(data, null, 2), {encoding: "utf-8"});
            log.debug("Saved!");
            log.debug("Finished.");
        } catch (err: any) {
            log.error(err);
        }
    }
}

export default Migrator;