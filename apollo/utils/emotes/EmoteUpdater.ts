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

import IArguments from "../../interfaces/IArguments";
import EventSource from "eventsource";
import IStorage from "../../interfaces/IStorage";
import { Logger } from "tslog";
import STVProvider from "emotelib/dist/providers/STVProvider";
import IEmote from "emotelib/dist/interfaces/IEmote";
import TwitchApi from "../../clients/ApiClient";

const log: Logger = new Logger({name: "EmoteUpdater"});
namespace EmoteUpdater {
    export class SevenTV {
        private emotes: {[target_name: string]: {[emote_name: string]: IStorage.Emote}};
        private targets: string[];
        private link: string;
        private src: EventSource;
        private dstv: STVProvider;

        constructor (stvprovider: STVProvider, channels: string[]) {
            this.dstv = stvprovider;
            this.emotes = {};
            this.targets = [];
            this.link = "";
            this.targets.forEach((target, index) => {
                if (index == 0) {
                    this.link = this.link + "?channel=" + target;
                } else {
                    this.link = this.link + "&channel=" + target;
                }
            });

            this.src = new EventSource("https://events.7tv.app/v1/channel-emotes" + this.link);
        }

        getEmote(emote_name: string, target_name: string) { return this.emotes[target_name][emote_name]; }
        getAllChannelEmotes(target_name: string) { return this.emotes[target_name]; }

        async levelUpEmote(message: string, target_name: string) {
            var _message: string[] = message.split(' ');

            _message.forEach((word) => {
                if (word in this.emotes[target_name]) this.emotes[target_name][word].UsedTimes = this.emotes[target_name][word].UsedTimes + 1;
            });
        }

        async fillUpEmotes(target_name: string) {
            try {
                const emotes: IEmote.STV[] | null = await this.dstv.getChannelEmotes(target_name);
                
                if (emotes === null) return false;

                emotes.forEach((emote) => {
                    this.newEmote(emote.name!, target_name, {
                        ID: emote.id,
                        UsedTimes: 0
                    });
                });
                
                return true;
            } catch (err: any) {
                log.error(err);
            }
        }

        async load(raw_targets: {[target_id: string]: IStorage.Target}) {
            Object.keys(raw_targets).forEach(async (id) => {
                // If channel already has at least one emote:
                if ("stv" in raw_targets[id].Emotes!) {
                    this.emotes[raw_targets[id].Name!] = raw_targets[id].Emotes!["stv"];
                    return false;
                }

                this.emotes[raw_targets[id].Name!] = {};
                await this.fillUpEmotes(raw_targets[id].Name!);
            });
        }

        removeEmote(emote_name: string, target_name: string) {
            if (!this.isEmoteExists(emote_name, target_name)) return false;
            delete this.emotes[target_name][emote_name];
            return true;
        }

        updateEmoteName(old_emote_name: string, emote_name: string, target_name: string) {
            if (!(old_emote_name in this.emotes[target_name])) return false;

            var old_emote: IStorage.Emote = this.emotes[target_name][old_emote_name];

            this.newEmote(emote_name, target_name, old_emote);
            this.removeEmote(old_emote_name, target_name);
        }

        newEmote(emote_name: string, target_name: string, data?: IStorage.Emote | undefined) {
            //if (!this.isEmoteExists(emote_name, target_name)) return false;

            if (data === undefined) {
                data = {
                    UsedTimes: 0
                }
            }


            this.emotes[target_name][emote_name] = data;
            return true;
        }

        newTargetEmote(target_name: string, emotes?: {[emote_name: string]: IStorage.Emote}) {
            if (this.isTargetExists(target_name)) return false;

            if (emotes === undefined) {
                emotes = {}
            }

            this.emotes[target_name] = emotes;
            return true;
        }

        isEmoteExists(emote_name: string, target_name: string) {
            if (!this.isTargetExists(target_name)) return false;
            if (!(emote_name in this.emotes[target_name])) return false;
            return true;
        }

        isTargetExists(target_name: string) {
            return target_name in this.emotes;
        }

        subscribe(args: IArguments) {
            this.src.addEventListener(
                "ready",
                (e) => {
                  // Should be "7tv-event-sub.v1" since this is the `v1` endpoint
                  console.log(e.data);
                }
              );
              
              this.src.addEventListener(
                "update",
                (e) => {
                  // This is a JSON payload matching the type for the specified event channel
                  var data: {[key: string]: any} = JSON.parse(e.data);

                  console.log(data);

                  switch (data.action) {
                    case "ADD":
                        this.newEmote(data.name, data.channel, {ID: data.id, UsedTimes: 0});
                        args.client.action(`#${data.channel}`, args.localizator.parsedText("emoteupdater.user_added_emote", data.channel, "[7TV]", data.actor, data.name));
                        break;
                    case "REMOVE":
                        this.removeEmote(data.name, data.channel);
                        args.client.action(`#${data.channel}`, args.localizator.parsedText("emoteupdater.user_deleted_emote", data.channel, "[7TV]", data.actor, data.name));
                        break;
                    case "UPDATE":
                        this.updateEmoteName(data.emote.name, data.name, data.channel);
                        args.client.action(`#${data.channel}`, args.localizator.parsedText("emoteupdater.user_updated_emote", data.channel, "[7TV]", data.actor, data.emote.name, data.name));
                        break;
                    default:
                        break;
                  }
                }
              );
              
              this.src.addEventListener(
                "open",
                (e) => {
                  // Connection was opened.
                }
              );
              
              this.src.addEventListener(
                "error",
                (e) => {
                }
              );
        }

        get getEmotes() { return this.emotes; }
        get getSubscribedTargets() { return this.targets; }
    }
}

export default EmoteUpdater;