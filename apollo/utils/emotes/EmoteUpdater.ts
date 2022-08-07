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

import IStorage from "../../interfaces/IStorage";
import { Logger } from "tslog";
import STVProvider from "emotelib/dist/providers/STVProvider";
import IEmote from "emotelib/dist/interfaces/IEmote";
import TwitchApi from "../../clients/ApiClient";
import { Client } from "tmi.js";
import Localizator from "../Locale";
import WebSocket from "ws";
import EmoteLib from "emotelib";
import TTVProvider from "emotelib/dist/providers/TTVProvider";
import BTTVProvider from "emotelib/dist/providers/BTTVProvider";
import FFZProvider from "emotelib/dist/providers/FFZProvider";

const log: Logger = new Logger({name: "EmoteUpdater"});

type EmoteProviders = "ttv" | "bttv" | "ffz" | "stv";

interface EmoteEventUpdate {
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
  
interface ExtraEmoteData {
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

class EmoteUpdater {
    /** Dictionary of all emotes from all provided channels. */
    private emotes: {
        [target_id: string]: {
            [provider_id: string]: IStorage.Emote[]
        }
    };

    private symlink: {
        [target_name: string]: string
    };

    private emotelib: EmoteLib;
    private tmi_client: Client;
    private ttv_api: TwitchApi.Client;
    private websocket: WebSocket | null;
    private localizator: Localizator;

    /**
     * Emote updater for 7TV, BetterTTV, FrankerFaceZ providers.
     * @param options Emote updater options.
     */
    constructor (options: {
        identify: {
            access_token: string,
            client_id: string
        },
        services: {
            client: Client,
            twitch_api: TwitchApi.Client,
            localizator: Localizator
        }
    }) {
        this.websocket = null;
        this.localizator = options.services.localizator;
        this.emotes = {};
        this.symlink = {};
        this.emotelib = new EmoteLib({
            access_token: options.identify.access_token,
            client_id: options.identify.client_id
        });
        this.ttv_api = options.services.twitch_api;
        this.tmi_client = options.services.client;
    }

    /**
     * Increase the "UsedTimes" key for emote.
     * @param text Text.
     * @param target_id Channel ID.
     */
    public increaseEmoteCount(text: string, target_id: string) {
        const _text: string[] = text.split(' ');

        for (const word of _text) {
            const stv_emote: IStorage.Emote | undefined = this.emotes[target_id]["stv"].find(e => e.Name === word);
            const bttv_emote: IStorage.Emote | undefined = this.emotes[target_id]["bttv"].find(e => e.Name === word);
            const ffz_emote: IStorage.Emote | undefined = this.emotes[target_id]["ffz"].find(e => e.Name === word);
            const ttv_emote: IStorage.Emote | undefined = this.emotes[target_id]["ttv"].find(e => e.Name === word);

            if (stv_emote !== undefined) this.setEmote(target_id, stv_emote.ID, "stv", "UsedTimes", stv_emote.UsedTimes + 1);
            if (bttv_emote !== undefined) this.setEmote(target_id, bttv_emote.ID, "bttv", "UsedTimes", bttv_emote.UsedTimes + 1);
            if (ffz_emote !== undefined) this.setEmote(target_id, ffz_emote.ID, "ffz", "UsedTimes", ffz_emote.UsedTimes + 1);
            if (ttv_emote !== undefined) this.setEmote(target_id, ttv_emote.ID, "ttv", "UsedTimes", ttv_emote.UsedTimes + 1);
        }
    }

    /**
     * Erase the existed data and load new for the emote updater.
     * @param targets Channels.
     */
    public async load(targets: {[target_id: string]: IStorage.Target}) {
        // Resetting the exist emotes, channel ids and etc.
        this.emotes = {};
        this.symlink = {};

        // Converting user ID to user names. This is needed for 7TV EventAPI.
        for await (const target_id of Object.keys(targets)) {
            const user = await this.ttv_api.getUserById(parseInt(target_id));
            if (user === undefined) return;

            this.symlink[user.login] = target_id;
            log.debug("ID", target_id, "-> username", user.login);
        }

        log.debug("Successfully converted user IDs to usernames!");

        // Getting the emotes from raw targets:
        for (const id of Object.keys(targets)) {
            // If target don't have any emotes, so just return.
            if (targets[id].Emotes === undefined) return;
            
            // Creating a new target in emote dictionary:
            this.emotes[id] = {};
            
            // Loading the emotes:
            if (targets[id].Emotes!["stv"] !== undefined) {
                this.emotes[id]["stv"] = targets[id].Emotes!["stv"];
            }
            if (targets[id].Emotes!["bttv"] !== undefined) {
                this.emotes[id]["bttv"] = targets[id].Emotes!["bttv"];
            }
            if (targets[id].Emotes!["ffz"] !== undefined) {
                this.emotes[id]["ffz"] = targets[id].Emotes!["ffz"];
            }
            if (targets[id].Emotes!["ttv"] !== undefined) {
                this.emotes[id]["ttv"] = targets[id].Emotes!["ttv"];
            }

            log.debug("Loaded the emotes for ID", id);
        }

        log.debug("Loaded all the emotes!");
    }

    public async subscribeTo7TVEventAPI() {
        if (this.websocket === null) {
            this.websocket = new WebSocket("wss://events.7tv.app/v1/channel-emotes");
        } else {
            this.websocket.close();
            this.websocket = new WebSocket("wss://events.7tv.app/v1/channel-emotes");
        }

        this.websocket.addEventListener("open", (event) => {
            log.debug("Connection to 7TV EventAPI is open!");
            for (const username of Object.keys(this.symlink)) {
                this.websocket!.send(JSON.stringify({
                    action: "join",
                    payload: username
                }));
            }
        });

        this.websocket.addEventListener("close", (event) => {
            log.debug("Connection to 7TV EventAPI is closed!", event.code, event.reason);
        });

        this.websocket.addEventListener("error", (event) => {
            log.debug("Error occurred in 7TV connection:", event.message);
        });

        this.websocket.addEventListener("message", (event) => {
            const data: {action: string, payload: string} = JSON.parse(event.data.toString());
                
            if (data.action == "ping") return;

            if (data.action == "update") {
                const emote: EmoteEventUpdate = JSON.parse(data.payload);

                switch (emote.action) {
                    case "ADD":
                        this.addEmote(this.symlink[emote.channel], emote.emote_id, "stv", {
                            ID: emote.emote_id,
                            Name: emote.name,
                            UsedTimes: 0
                        });

                        this.tmi_client.action(`#${emote.channel}`, this.localizator.parsedText("emoteupdater.user_added_emote", undefined, ["[7TV]", emote.actor, emote.name], {
                            username: emote.channel
                        }));
                        break;
                    case "REMOVE":
                        this.removeEmote(this.symlink[emote.channel], emote.emote_id, "stv", true);
                        
                        this.tmi_client.action(`#${emote.channel}`, this.localizator.parsedText("emoteupdater.user_deleted_emote", undefined, ["[7TV]", emote.actor, emote.name], {
                            username: emote.channel
                        }));
                        break;
                    case "UPDATE":
                        var _emote: IStorage.Emote | null = this.getEmote(this.symlink[emote.channel], emote.emote_id, "stv");

                        this.tmi_client.action(`#${emote.channel}`, this.localizator.parsedText("emoteupdater.user_updated_emote", undefined, ["[7TV]", emote.actor, (_emote !== null) ? _emote.Name : emote.emote?.name, emote.name], {
                            username: emote.channel
                        }));

                        if (_emote !== null) {
                            if (_emote.NameHistory === undefined) _emote.NameHistory = [];
                            _emote.NameHistory.push(_emote.Name);

                            _emote.Name = emote.name;
                        }
                        
                        break;
                    default:
                        break;
                  }
            }
        });
    }

    private announce(target_name: string, provider: string, new_emotes: string[], deleted_emotes: string[]) {
        if (new_emotes.length > 0) this.tmi_client.say(`#${target_name}`, this.localizator.parsedText("emoteupdater.new_emotes", undefined, [
            provider,
            new_emotes.join(' ')
        ], {
            username: target_name
        }));
        if (deleted_emotes.length > 0) this.tmi_client.say(`#${target_name}`, this.localizator.parsedText("emoteupdater.deleted_emotes", undefined, [
            provider,
            deleted_emotes.join(' ')
        ], {
            username: target_name
        }));
    }

    /**
     * Synchronize local emotes with channel/global BetterTTV emotes.
     * @param target_name Channel name.
     * @param announce Announce changes in the chat room?
     */
    public async syncBTTVEmotes(target_name: string, announce?: boolean | undefined) {
        const provider: BTTVProvider = this.emotelib["betterttv"];
        if (this.symlink[target_name] === undefined) throw new Error("Username " + target_name + " not found in symlinks.");
        const channel_emotes: IEmote.BTTV[] | null = await provider.getChannelEmotes(this.symlink[target_name]);
        const global_emotes: IEmote.BTTV[] | null = await provider.getGlobalEmotes();
        var new_emotes: string[] = [];
        var deleted_emotes: string[] = [];

        if (channel_emotes === null) throw new Error("Channel ID " + target_name + " don't have any channel emotes.");
        if (global_emotes === null) throw new Error("No global emotes.");
        if (this.emotes[this.symlink[target_name]]["bttv"] === undefined) this.emotes[this.symlink[target_name]]["bttv"] = [];

        // New channel emotes:
        for (const emote of channel_emotes) {
            const _emote: IStorage.Emote | undefined = this.emotes[this.symlink[target_name]]["bttv"].find(e => e.ID == emote.id!);

            if (_emote === undefined) {
                this.addEmote(this.symlink[target_name], emote.id!, "bttv", {
                    ID: emote.id!,
                    Name: emote.code!,
                    UsedTimes: 0
                });
                new_emotes.push(emote.code!);
            }
        }

        // New global emotes:
        for (const emote of global_emotes) {
            const _emote: IStorage.Emote | undefined = this.emotes[this.symlink[target_name]]["bttv"].find(e => e.ID == emote.id!);

            if (_emote === undefined) {
                this.addEmote(this.symlink[target_name], emote.id!, "bttv", {
                    ID: emote.id!,
                    Name: emote.code!,
                    UsedTimes: 0,
                    isGlobal: true
                });
                new_emotes.push(emote.code!);
            }
        }

        // Deleted global/channel emotes:
        for (const emote of this.emotes[this.symlink[target_name]]["bttv"]) {
            const _emote: IEmote.BTTV | undefined = channel_emotes.find(e => e.id === emote.ID);
            const __emote: IEmote.BTTV | undefined = global_emotes.find(e => e.id === emote.ID);

            if (_emote === undefined && emote.isGlobal != true) {
                this.removeEmote(this.symlink[target_name], emote.ID, "bttv", true);
                deleted_emotes.push(emote.Name);
            }

            if (__emote === undefined && emote.isGlobal == true) {
                this.removeEmote(this.symlink[target_name], emote.ID, "bttv", true);
                deleted_emotes.push(emote.Name);
            }
        }
        // Announce changes in the chat:
        if (announce) {
            this.announce(target_name, "[BTTV]", new_emotes, deleted_emotes);
        }
    }

    /**
     * Synchronize local emotes with channel/global FrankerFaceZ emotes.
     * @param target_name Channel name.
     * @param announce Announce changes in the chat room?
     */
     public async syncFFZEmotes(target_name: string, announce?: boolean | undefined) {
        const provider: FFZProvider = this.emotelib["frankerfacez"];
        if (this.symlink[target_name] === undefined) throw new Error("Username " + target_name + " not found in symlinks.");
        const channel_emotes: IEmote.FFZ[] | null = await provider.getChannelEmotes(this.symlink[target_name]);
        const global_emotes: IEmote.FFZ[] | null = await provider.getGlobalEmotes();
        var new_emotes: string[] = [];
        var deleted_emotes: string[] = [];

        if (channel_emotes === null) throw new Error("Channel ID " + target_name + " don't have any channel emotes.");
        if (global_emotes === null) throw new Error("No global emotes.");
        if (this.emotes[this.symlink[target_name]]["ffz"] === undefined) this.emotes[this.symlink[target_name]]["ffz"] = [];

        // New channel emotes:
        for (const emote of channel_emotes) {
            const _emote: IStorage.Emote | undefined = this.emotes[this.symlink[target_name]]["ffz"].find(e => e.ID == emote.id!.toString());

            if (_emote === undefined) {
                this.addEmote(this.symlink[target_name], emote.id!.toString(), "ffz", {
                    ID: emote.id!.toString(),
                    Name: emote.name!,
                    UsedTimes: 0
                });
                new_emotes.push(emote.name!);
            }
        }

        // New global emotes:
        for (const emote of global_emotes) {
            const _emote: IStorage.Emote | undefined = this.emotes[this.symlink[target_name]]["ffz"].find(e => e.ID == emote.id!.toString());

            if (_emote === undefined) {
                this.addEmote(this.symlink[target_name], emote.id!.toString(), "ffz", {
                    ID: emote.id!.toString(),
                    Name: emote.name!,
                    UsedTimes: 0,
                    isGlobal: true
                });
                new_emotes.push(emote.name!);
            }
        }

        // Deleted global/channel emotes:
        for (const emote of this.emotes[this.symlink[target_name]]["ffz"]) {
            const _emote: IEmote.FFZ | undefined = channel_emotes.find(e => e.id!.toString() === emote.ID);
            const __emote: IEmote.FFZ | undefined = global_emotes.find(e => e.id!.toString() === emote.ID);

            if (_emote === undefined && emote.isGlobal != true) {
                this.removeEmote(this.symlink[target_name], emote.ID, "ffz", true);
                deleted_emotes.push(emote.Name);
            }

            if (__emote === undefined && emote.isGlobal == true) {
                this.removeEmote(this.symlink[target_name], emote.ID, "ffz", true);
                deleted_emotes.push(emote.Name);
            }
        }

        // Announce changes in the chat:
        if (announce) {
            this.announce(target_name, "[FFZ]", new_emotes, deleted_emotes);
        }
    }

    /**
     * Synchronize local emotes with channel/global Twitch emotes.
     * @param target_name Channel name.
     * @param announce Announce changes in the chat room?
     */
     public async syncTTVEmotes(target_name: string, announce?: boolean | undefined) {
        const provider: TTVProvider = this.emotelib["twitch"];
        if (this.symlink[target_name] === undefined) throw new Error("Username " + target_name + " not found in symlinks.");
        const channel_emotes: IEmote.TTV[] | null = await provider.getChannelEmotes(this.symlink[target_name]);
        const global_emotes: IEmote.TTV[] | null = await provider.getGlobalEmotes();
        var new_emotes: string[] = [];
        var deleted_emotes: string[] = [];

        if (channel_emotes === null) throw new Error("Channel ID " + target_name + " don't have any channel emotes.");
        if (global_emotes === null) throw new Error("No global emotes.");
        if (this.emotes[this.symlink[target_name]]["ttv"] === undefined) this.emotes[this.symlink[target_name]]["ttv"] = [];

        // New channel emotes:
        for (const emote of channel_emotes) {
            const _emote: IStorage.Emote | undefined = this.emotes[this.symlink[target_name]]["ttv"].find(e => e.ID == emote.id!);

            if (_emote === undefined) {
                this.addEmote(this.symlink[target_name], emote.id!, "ttv", {
                    ID: emote.id!,
                    Name: emote.name!,
                    UsedTimes: 0
                });
                new_emotes.push(emote.name!);
            }
        }

        // New global emotes:
        for (const emote of global_emotes) {
            const _emote: IStorage.Emote | undefined = this.emotes[this.symlink[target_name]]["ttv"].find(e => e.ID == emote.id!);

            if (_emote === undefined) {
                this.addEmote(this.symlink[target_name], emote.id!, "ttv", {
                    ID: emote.id!,
                    Name: emote.name!,
                    UsedTimes: 0,
                    isGlobal: true
                });
                new_emotes.push(emote.name!);
            }
        }

        // Deleted global/channel emotes:
        for (const emote of this.emotes[this.symlink[target_name]]["ttv"]) {
            const _emote: IEmote.TTV | undefined = channel_emotes.find(e => e.id! === emote.ID);
            const __emote: IEmote.TTV | undefined = global_emotes.find(e => e.id! === emote.ID);

            if (_emote === undefined && emote.isGlobal != true) {
                this.removeEmote(this.symlink[target_name], emote.ID, "ttv", true);
                deleted_emotes.push(emote.Name);
            }

            if (__emote === undefined && emote.isGlobal == true) {
                this.removeEmote(this.symlink[target_name], emote.ID, "ttv", true);
                deleted_emotes.push(emote.Name);
            }
        }

        // Announce changes in the chat:
        if (announce) {
            this.announce(target_name, "[Twitch]", new_emotes, deleted_emotes);
        }
    }

    /**
     * Synchronize local emotes with channel/global 7TV emotes.
     * @param target_name Channel name.
     * @param announce Announce changes in the chat room?
     */
     public async sync7TVEmotes(target_name: string, announce?: boolean | undefined) {
        const provider: STVProvider = this.emotelib["seventv"];
        if (this.symlink[target_name] === undefined) throw new Error("Username " + target_name + " not found in symlinks.");
        const channel_emotes: IEmote.STV[] | null = await provider.getChannelEmotes(this.symlink[target_name]);
        const global_emotes: IEmote.STV[] | null = await provider.getGlobalEmotes();
        var new_emotes: string[] = [];
        var deleted_emotes: string[] = [];

        if (channel_emotes === null) throw new Error("Channel ID " + target_name + " don't have any channel emotes.");
        if (global_emotes === null) throw new Error("No global emotes.");
        if (this.emotes[this.symlink[target_name]]["stv"] === undefined) this.emotes[this.symlink[target_name]]["stv"] = [];

        // New channel emotes:
        for (const emote of channel_emotes) {
            const _emote: IStorage.Emote | undefined = this.emotes[this.symlink[target_name]]["stv"].find(e => e.ID == emote.id!);

            if (_emote === undefined) {
                this.addEmote(this.symlink[target_name], emote.id!, "stv", {
                    ID: emote.id!,
                    Name: emote.name!,
                    UsedTimes: 0
                });
                new_emotes.push(emote.name!);
            }
        }

        // New global emotes:
        for (const emote of global_emotes) {
            const _emote: IStorage.Emote | undefined = this.emotes[this.symlink[target_name]]["stv"].find(e => e.ID == emote.id!);

            if (_emote === undefined) {
                this.addEmote(this.symlink[target_name], emote.id!, "stv", {
                    ID: emote.id!,
                    Name: emote.name!,
                    UsedTimes: 0,
                    isGlobal: true
                });
                new_emotes.push(emote.name!);
            }
        }

        // Deleted global/channel emotes:
        for (const emote of this.emotes[this.symlink[target_name]]["stv"]) {
            const _emote: IEmote.STV | undefined = channel_emotes.find(e => e.id === emote.ID);
            const __emote: IEmote.STV | undefined = global_emotes.find(e => e.id === emote.ID);

            if (_emote === undefined && emote.isGlobal != true) {
                this.removeEmote(this.symlink[target_name], emote.ID, "stv", true);
                deleted_emotes.push(emote.Name);
            }

            if (__emote === undefined && emote.isGlobal == true) {
                this.removeEmote(this.symlink[target_name], emote.ID, "stv", true);
                deleted_emotes.push(emote.Name);
            }
        }

        // Announce changes in the chat:
        if (announce) {
            this.announce(target_name, "[7TV]", new_emotes, deleted_emotes);
        }
    }

    /**
     * Add new emote entry in the dictionary.
     * @param target_id Channel ID.
     * @param emote_id Emote ID that provided by Emote Proivder.
     * @param provider_id Emote provider.
     * @param data Emote data.
     */
    public addEmote(
        target_id: string,
        emote_id: string,
        provider_id: "ttv" | "bttv" | "ffz" | "stv",
        data: IStorage.Emote
    ): void {
        if (!this.containsTarget(target_id)) throw new Error(`Target ID ${target_id} not in emote dictionary.`);
        if (!this.containsProvider(target_id, provider_id)) throw new Error(`Provider ID ${provider_id} not in ${target_id}'s emote dictionary.`);
        this.emotes[target_id][provider_id].push(data);
    }

    /**
     * Remove the emote entry from the dictionary.
     * @param target_id Channel ID.
     * @param emote_id Emote ID that provided by Emote Proivder.
     * @param provider_id Emote provider.
     */
    public removeEmote(
        target_id: string,
        emote_id: string,
        provider_id: "ttv" | "bttv" | "ffz" | "stv",
        deletionTag?: boolean | undefined
    ): void {
        var emote: IStorage.Emote | null = this.getEmote(target_id, emote_id, provider_id);

        if (emote === null) throw new Error(`Emote ID ${emote_id} (${provider_id}) not in ${target_id}'s emote dictionary.`);

        if (deletionTag === undefined) deletionTag = true;

        if (deletionTag) {
            emote.isDeleted = true;
        } else {
            delete this.emotes[target_id][provider_id][this.emotes[target_id][provider_id].indexOf(emote)];

            // Removing the empty items:
            this.emotes[target_id][provider_id] = this.emotes[target_id][provider_id].filter(e => e !== null);
        }
    }

    /**
     * Set value in emote entry in the dictionary.
     * @param target_id Channel ID.
     * @param emote_id Emote ID that provided by Emote Proivder.
     * @param provider_id Emote provider.
     * @param key Key to edit.
     * @param value Value to set for key. If undefined, it will remove the key.
     */
    public setEmote<T extends keyof IStorage.Emote>(
        target_id: string,
        emote_id: string,
        provider_id: EmoteProviders,
        key: T,
        value: IStorage.Emote[T]
    ): void {
        if (!this.containsTarget(target_id)) throw new Error(`Target ID ${target_id} not in emote dictionary.`);
        if (!this.containsProvider(target_id, provider_id)) throw new Error(`Provider ID ${provider_id} not in ${target_id}'s emote dictionary.`);
        
        var emote: IStorage.Emote | null = this.getEmote(target_id, emote_id, provider_id);

        if (emote === null) throw new Error(`Emote ID ${emote_id} (${provider_id}) not in ${target_id}'s emote dictionary.`);

        if (value === undefined) {
            delete emote[key];
            return;
        }

        emote[key] = value;
    }

    /**
     * Get an emote.
     * @param target_id Channel ID.
     * @param emote_id Emote ID.
     * @param provider_id Provider ID.
     * @returns IStorage.Emote if emote found. If not, just returns null.
     */
    public getEmote(
        target_id: string,
        emote_id: string,
        provider_id: EmoteProviders
    ): IStorage.Emote | null {
        if (!this.containsTarget(target_id)) throw new Error(`Target ID ${target_id} not in emote dictionary.`);
        if (!this.containsProvider(target_id, provider_id)) throw new Error(`Provider ID ${provider_id} not in ${target_id}'s emote dictionary.`);

        var emote: IStorage.Emote | null = null;

        for (const _emote of this.emotes[target_id][provider_id]) {
            if (emote_id === _emote.ID || emote_id === _emote.Name) {
                emote = _emote;
            }
        }

        return emote;
    }

    public containsTarget(target_id: string): boolean {
        if (!(target_id in this.emotes)) return false;
        return true;
    }

    public containsProvider(target_id: string, provider_id: EmoteProviders): boolean {
        if (!(provider_id in this.emotes[target_id])) return false;
        return true;
    }

    /**
     * Get the all channel emotes.
     * @param target_id Channel ID.
     * @param provider_id Provider ID.
     * @returns emotes of provider if it specified. If not, returns the full list of emotes of every provider.
     */
    public getChannelEmotes(target_id: string, provider_id?: EmoteProviders | undefined): IStorage.Emote[] | {[provider_id: string]: IStorage.Emote[]} { 
        if (provider_id !== undefined) return this.emotes[target_id][provider_id];
        return this.emotes[target_id];
    }

    public get getEmotes() { return this.emotes; }
}

export default EmoteUpdater;