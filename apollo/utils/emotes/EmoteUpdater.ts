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
import { Client } from "tmi.js";
import Localizator from "../Locale";
import WebSocket from "ws";

const log: Logger = new Logger({name: "EmoteUpdater"});
namespace EmoteUpdater {
    export interface EmoteEventUpdate {
        // The channel this update affects.
        channel: string;
        // The ID of the emote.
        emote_id: string;
        // The name or channel alias of the emote.
        name: string;
        // The action done.
        action: "ADD" | "REMOVE" | "UPDATE";
        // The user who caused this event to trigger.
        actor: string;
        // An emote object. Null if the action is "REMOVE".
        emote?: ExtraEmoteData;
      }
      
      export interface ExtraEmoteData {
        // Original name of the emote.
        name: string;
        // The visibility bitfield of this emote.
        visibility: number;
        // The MIME type of the images.
        mime: string;
        // The TAGs on this emote.
        tags: string[];
        // The widths of the images.
        width: [number, number, number, number];
        // The heights of the images.
        height: [number, number, number, number];
        // The animation status of the emote.
        animated: boolean;
        // Infomation about the uploader.
        owner: {
          // 7TV ID of the owner.
          id: string;
          // Twitch ID of the owner.
          twitch_id: string;
          // Twitch DisplayName of the owner.
          display_name: string;
          // Twitch Login of the owner.
          login: string;
        };
        // The first string in the inner array will contain the "name" of the URL, like "1" or "2" or "3" or "4"
        // or some custom event names we haven't figured out yet such as "christmas_1" or "halloween_1" for special versions of emotes.
        // The second string in the inner array will contain the actual CDN URL of the emote. You should use these URLs and not derive URLs
        // based on the emote ID and size you want, since in future we might add "custom styles" and this will allow you to easily update your app,
        // and solve any future breaking changes you apps might receive due to us changing.
        urls: [[string, string]];
    }
    export class SevenTV {
        private emotes: {[target_name: string]: {[emote_name: string]: IStorage.Emote}};
        private dstv: STVProvider;
        private sub7tv: EventSource | null;
        private socket: WebSocket | null;

        constructor (stvprovider: STVProvider, channels: string[]) {
            this.dstv = stvprovider;
            this.emotes = {};
            this.sub7tv = null;
            this.socket = null;
        }

        /**
         * Join the channel to 7TV EventAPI via WebSocket.
         * @param target_name 
         * @returns False if WebSocket doesn't exists.
         */
        join(target_name: string) {
            if (this.socket === null) return false;

            this.socket.send(JSON.stringify({
                action: "join",
                payload: target_name
            }, null, 2), (err) => {
                log.error("Error occurred while adding the channel", target_name, "to 7TV EventAPI via WebSocket:", err?.message);
            });
        }

        /**
         * Part the channel from 7TV EventAPI via WebSocket.
         * @param target_name 
         * @returns False if WebSocket doesn't exists.
         */
        part(target_name: string) {
            if (this.socket === null) return false;
            this.socket.send(JSON.stringify({
                action: "part",
                payload: target_name
            }, null, 2), (err) => {
                log.error("Error occurred while parting the channel", target_name, "from 7TV EventAPI via WebSocket:", err);
            });
        }

        /**
         * Subscribe to 7TV Event API via WebSocket.
         * @param client Tmi.js client.
         * @param locale Localizatior.
         * @param channels Channels that will be joined at startup.
         */
        subscribeToEmoteUpdates(client: Client, locale: Localizator, channels: string[]) {
            if (this.socket === null) {
                this.socket = new WebSocket("wss://events.7tv.app/v1/channel-emotes");
            } else {
                this.socket.close();
                this.socket = new WebSocket("wss://events.7tv.app/v1/channel-emotes");
            }

            this.socket.addEventListener("open", (event) => {
                if (this.socket === null) return;

                channels.forEach(async (channel) => {
                    this.join(channel);
                });
            });

            this.socket.addEventListener("message", (event) => {
                const data: {action: string, payload: string} = JSON.parse(event.data.toString());
                
                if (data.action == "ping") return;
                if (data.action == "update") {
                    const emote: EmoteEventUpdate = JSON.parse(data.payload);

                    switch (emote.action) {
                        case "ADD":
                            this.newEmote(emote.name, emote.channel, {ID: emote.emote_id, UsedTimes: 0});
                            client.action(`#${emote.channel}`, locale.parsedText("emoteupdater.user_added_emote", emote.channel, "[7TV]", emote.actor, emote.name));
                            break;
                        case "REMOVE":
                            this.removeEmote(emote.name, emote.channel);
                            client.action(`#${emote.channel}`, locale.parsedText("emoteupdater.user_deleted_emote", emote.channel, "[7TV]", emote.actor, emote.name));
                            break;
                        case "UPDATE":
                            this.updateEmoteName(emote.emote!.name, emote.name, emote.channel);
                            client.action(`#${emote.channel}`, locale.parsedText("emoteupdater.user_updated_emote", emote.channel, "[7TV]", emote.actor, emote.emote!.name, emote.name));
                            break;
                        default:
                            break;
                      }
                }
            });

            this.socket.addEventListener("error", (event) => {
                log.error(event.error);
            });

            this.socket.addEventListener("close", (event) => {
                log.debug(event);
            });
        }

        getEmote(emote_name: string, target_name: string) { 
            if (!(target_name in this.emotes)) return false;
            if (!(emote_name in this.emotes[target_name])) return false;
            return this.emotes[target_name][emote_name];
        }
        getAllChannelEmotes(target_name: string) { return this.emotes[target_name]; }

        async levelUpEmote(message: string, target_name: string) {
            var _message: string[] = message.split(' ');

            if (!(target_name in this.emotes)) return false;

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
  
        get getEmotes() { return this.emotes; }
    }
}

export default EmoteUpdater;