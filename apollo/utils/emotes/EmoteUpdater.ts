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
import { Emotes, PrismaClient, Target } from "@prisma/client";
import IServices from "../../interfaces/IServices";
import Symlinks from "../../files/Symlinks";
import IEmoteProvider from "emotelib/dist/interfaces/IEmoteProvider";

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
    private emotelib: EmoteLib;
    private tmi_client: Client;
    private ttv_api: TwitchApi.Client;
    private websocket: WebSocket | null;
    private localizator: Localizator;
    private db: PrismaClient;
    private symlinks: Symlinks;

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
            localizator: Localizator,
            db: PrismaClient,
            symlinks: Symlinks
        }
    }) {
        this.websocket = null;
        this.symlinks = options.services.symlinks;
        this.localizator = options.services.localizator;
        this.emotelib = new EmoteLib({
            access_token: options.identify.access_token,
            client_id: options.identify.client_id
        });
        this.ttv_api = options.services.twitch_api;
        this.tmi_client = options.services.client;
        this.db = options.services.db;
    }

    /**
     * Increase the "UsedTimes" key for emote.
     * @param text Text.
     * @param target_id Channel ID.
     */
    public async increaseEmoteCount(text: string, target_id: string) {
        const _text: string[] = text.split(' ');
        const user: Target | null = await this.db.target.findFirst({
            where: {
                alias_id: parseInt(target_id)
            }
        });

        if (user === null) return;

        for (const word of _text) {
            const stv_emote: Emotes | null = await this.db.emotes.findFirst({
                where: {
                    targetId: user.id,
                    name: word,
                    provider: "stv"
                }
            });

            const bttv_emote: Emotes | null = await this.db.emotes.findFirst({
                where: {
                    targetId: user.id,
                    name: word,
                    provider: "bttv"
                }
            });

            const ffz_emote: Emotes | null = await this.db.emotes.findFirst({
                where: {
                    targetId: user.id,
                    name: word,
                    provider: "ffz"
                }
            });

            const ttv_emote: Emotes | null = await this.db.emotes.findFirst({
                where: {targetId: user.id,
                    name: word,
                    provider: "ttv"
                }
            });

            if (stv_emote !== null) {
                await this.db.emotes.update({
                    where: {int_id: stv_emote.int_id},
                    data: {
                        used_times: stv_emote.used_times + 1
                    }
                });
            }
            if (bttv_emote !== null) {
                await this.db.emotes.update({
                    where: {int_id: bttv_emote.int_id},
                    data: {
                        used_times: bttv_emote.used_times + 1
                    }
                });
            }
            if (ffz_emote !== null) {
                await this.db.emotes.update({
                    where: {int_id: ffz_emote.int_id},
                    data: {
                        used_times: ffz_emote.used_times + 1
                    }
                });
            }
            if (ttv_emote !== null) {
                await this.db.emotes.update({
                    where: {int_id: ttv_emote.int_id},
                    data: {
                        used_times: ttv_emote.used_times + 1
                    }
                });
            }
        }
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
            for (const username of Object.keys(this.symlinks.getSymlinks())) {
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

        this.websocket.addEventListener("message", async (event) => {
            const data: {action: string, payload: string} = JSON.parse(event.data.toString());
                
            if (data.action == "ping") return;

            if (data.action == "update") {
                const emote: EmoteEventUpdate = JSON.parse(data.payload);
                const user: Target | null = await this.db.target.findFirst({
                    where: {alias_id: parseInt(this.symlinks.getSymlink(emote.channel)!)}
                });

                if (user === null) return;

                switch (emote.action) {
                    case "ADD":
                        const _eemote: Emotes | null = await this.db.emotes.findFirst({
                            where: {
                                name: emote.name,
                                id: emote.emote_id,
                                provider: "stv",
                                targetId: user.id
                            }
                        });

                        if (_eemote === null) {
                            await this.db.emotes.create({
                                data: {
                                    id: emote.emote_id,
                                    name: emote.name,
                                    provider: "stv",
                                    targetId: user.id
                                }
                            });
                        } else {
                            await this.db.emotes.update({
                                where: {int_id: _eemote.int_id},
                                data: {
                                    name: emote.name,
                                    is_deleted: false
                                }
                            });
                        }
                        
                        this.tmi_client.action(`#${emote.channel}`,
                        await this.localizator.parsedText("emoteupdater.user_added_emote", undefined, [
                            "[7TV]",
                            emote.actor,
                            emote.name
                        ], {
                            target_name: emote.channel
                        }));
                        break;
                    case "REMOVE":
                        const _emote: Emotes | null = await this.db.emotes.findFirst({
                            where: {
                                name: emote.name,
                                id: emote.emote_id,
                                provider: "stv",
                                targetId: user.id
                            }
                        });

                        if (_emote === null) return;

                        await this.db.emotes.update({
                            where: {int_id: _emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                        
                        this.tmi_client.action(`#${emote.channel}`,
                        await this.localizator.parsedText("emoteupdater.user_deleted_emote", undefined, [
                            "[7TV]",
                            emote.actor,
                            emote.name
                        ], {
                            target_name: emote.channel
                        }));
                        break;
                    case "UPDATE":
                        var _emote_: Emotes | null = await this.db.emotes.findFirst({
                            where: {
                                id: emote.emote_id,
                                provider: "stv",
                                targetId: user.id
                            }
                        });

                        this.tmi_client.action(`#${emote.channel}`,
                        await this.localizator.parsedText("emoteupdater.user_updated_emote", undefined, [
                            "[7TV]",
                            emote.actor,
                            (_emote_ !== null) ? _emote_.name : emote.emote?.name, emote.name
                        ], {
                            target_name: emote.channel
                        }));

                        if (_emote_ !== null) {
                            await this.db.emoteNameHistory.create({data: {
                                id: _emote_.id,
                                target_id: user.id,
                                name: _emote_.name
                            }});

                            await this.db.emotes.update({
                                where: {
                                    int_id: _emote_.int_id
                                },
                                data: {
                                    name: emote.name
                                }
                            });
                        }
                        break;
                    default:
                        break;
                  }
            }
        });
    }

    /**
     * Synchronize emotes of all providers.
     * @param target_id Target ID.
     */
    public async syncAllEmotes(target_id: string): Promise<void> {
        if (parseInt(target_id) < 0) return;

        const target: Target | null = await this.db.target.findFirst({
            where: {alias_id: parseInt(target_id)}
        });

        if (!target) return;

        const channel_emotes: {
            bttv: IEmote.BTTV[] | null;
            ffz: IEmote.FFZ[] | null;
            stv: IEmote.STV[] | null;
            ttv: IEmote.TTV[] | null;
        } = {
            bttv: await this.emotelib.betterttv.getChannelEmotes(target_id),
            ffz: await this.emotelib.frankerfacez.getChannelEmotes(target_id),
            stv: await this.emotelib.seventv.getChannelEmotes(target_id),
            ttv: await this.emotelib.twitch.getChannelEmotes(target_id)
        }

        const global_emotes: {
            bttv: IEmote.BTTV[] | null;
            ffz: IEmote.FFZ[] | null;
            stv: IEmote.STV[] | null;
            ttv: IEmote.TTV[] | null;
        } = {
            bttv: await this.emotelib.betterttv.getGlobalEmotes(),
            ffz: await this.emotelib.frankerfacez.getGlobalEmotes(),
            stv: await this.emotelib.seventv.getGlobalEmotes(),
            ttv: await this.emotelib.twitch.getGlobalEmotes()
        }

        const emotes: Emotes[] | null = await this.db.emotes.findMany({
            where: {
                targetId: target.id
            }
        });

        // -- New BTTV channel emotes:
        if (channel_emotes.bttv) {
            for (const emote of channel_emotes.bttv) {
                const _emote = await this.db.emotes.findFirst({
                    where: {
                        targetId: target.id,
                        id: emote.id,
                        provider: "bttv"
                    }
                });

                if (!_emote) {
                    await this.db.emotes.create({
                        data: {
                            id: emote.id!,
                            name: emote.code!,
                            targetId: target.id,
                            provider: "bttv"
                        }
                    });
                }
            }

            if (emotes) {
                for (const emote of emotes.filter(e => e.provider === "bttv")) {
                    if (emote.is_global) return;
                    const __emote: IEmote.BTTV | undefined = channel_emotes.bttv.find(e => e.id === emote.id);
                    
                    if (!__emote) {
                        await this.db.emotes.update({
                            where: {int_id: emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                    }
                }
            }
        }

        // -- New FFZ channel emotes:
        if (channel_emotes.ffz) {
            for (const emote of channel_emotes.ffz) {
                const _emote = await this.db.emotes.findFirst({
                    where: {
                        targetId: target.id,
                        id: emote.id!.toString(),
                        provider: "ffz"
                    }
                });

                if (!_emote) {
                    await this.db.emotes.create({
                        data: {
                            id: emote.id!.toString(),
                            name: emote.name!,
                            targetId: target.id,
                            provider: "ffz"
                        }
                    });
                }
            }

            if (emotes) {
                for (const emote of emotes.filter(e => e.provider === "ffz")) {
                    if (emote.is_global) return;
                    const __emote: IEmote.FFZ | undefined = channel_emotes.ffz.find(e => e.id === parseInt(emote.id));
                    
                    if (!__emote) {
                        await this.db.emotes.update({
                            where: {int_id: emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                    }
                }
            }
        }

        // -- New TTV channel emotes:
        if (channel_emotes.ttv) {
            for (const emote of channel_emotes.ttv) {
                const _emote = await this.db.emotes.findFirst({
                    where: {
                        targetId: target.id,
                        id: emote.id,
                        provider: "ttv"
                    }
                });

                if (!_emote) {
                    await this.db.emotes.create({
                        data: {
                            id: emote.id!,
                            name: emote.name!,
                            targetId: target.id,
                            provider: "ttv"
                        }
                    });
                }
            }

            if (emotes) {
                for (const emote of emotes.filter(e => e.provider === "ttv")) {
                    if (emote.is_global) return;
                    const __emote: IEmote.TTV | undefined = channel_emotes.ttv.find(e => e.id === emote.id);
                    if (!__emote) {
                        await this.db.emotes.update({
                            where: {int_id: emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                    }
                }
            }
        }

        // -- New 7TV channel emotes:
        if (channel_emotes.stv) {
            for (const emote of channel_emotes.stv) {
                const _emote = await this.db.emotes.findFirst({
                    where: {
                        targetId: target.id,
                        id: emote.id,
                        provider: "stv"
                    }
                });

                if (!_emote) {
                    await this.db.emotes.create({
                        data: {
                            id: emote.id!,
                            name: emote.name!,
                            targetId: target.id,
                            provider: "stv"
                        }
                    });
                }
            }

            if (emotes) {
                for (const emote of emotes.filter(e => e.provider === "stv")) {
                    if (emote.is_global) return;
                    const __emote: IEmote.STV | undefined = channel_emotes.stv.find(e => e.id === emote.id);
                    
                    if (!__emote) {
                        await this.db.emotes.update({
                            where: {int_id: emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                    }
                }
            }
        }
        ///////////////////////////////////////////
        // -- New BTTV global emotes:
        if (global_emotes.bttv) {
            for (const emote of global_emotes.bttv) {
                const _emote = await this.db.emotes.findFirst({
                    where: {
                        targetId: target.id,
                        id: emote.id,
                        provider: "bttv",
                        is_global: true
                    }
                });

                if (!_emote) {
                    await this.db.emotes.create({
                        data: {
                            id: emote.id!,
                            name: emote.code!,
                            targetId: target.id,
                            provider: "bttv",
                            is_global: true
                        }
                    });
                }
            }

            if (emotes) {
                for (const emote of emotes.filter(e => e.provider === "bttv")) {
                    if (!emote.is_global) return;
                    const __emote: IEmote.BTTV | undefined = global_emotes.bttv.find(e => e.id === emote.id);
                    
                    if (!__emote) {
                        await this.db.emotes.update({
                            where: {int_id: emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                    }
                }
            }
        }

        // -- New FFZ global emotes:
        if (global_emotes.ffz) {
            for (const emote of global_emotes.ffz) {
                const _emote = await this.db.emotes.findFirst({
                    where: {
                        targetId: target.id,
                        id: emote.id!.toString(),
                        provider: "ffz",
                        is_global: true
                    }
                });

                if (!_emote) {
                    await this.db.emotes.create({
                        data: {
                            id: emote.id!.toString(),
                            name: emote.name!,
                            targetId: target.id,
                            provider: "ffz",
                            is_global: true
                        }
                    });
                }
            }

            if (emotes) {
                for (const emote of emotes.filter(e => e.provider === "ffz")) {
                    if (!emote.is_global) return;
                    const __emote: IEmote.FFZ | undefined = global_emotes.ffz.find(e => e.id === parseInt(emote.id));
                    
                    if (!__emote) {
                        await this.db.emotes.update({
                            where: {int_id: emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                    }
                }
            }
        }

        // -- New TTV global emotes:
        if (global_emotes.ttv) {
            for (const emote of global_emotes.ttv) {
                const _emote = await this.db.emotes.findFirst({
                    where: {
                        targetId: target.id,
                        id: emote.id,
                        provider: "ttv",
                        is_global: true
                    }
                });

                if (!_emote) {
                    await this.db.emotes.create({
                        data: {
                            id: emote.id!,
                            name: emote.name!,
                            targetId: target.id,
                            provider: "ttv",
                            is_global: true
                        }
                    });
                }
            }

            if (emotes) {
                for (const emote of emotes.filter(e => e.provider === "ttv")) {
                    if (!emote.is_global) return;
                    const __emote: IEmote.TTV | undefined = global_emotes.ttv.find(e => e.id === emote.id);
                    
                    if (!__emote) {
                        await this.db.emotes.update({
                            where: {int_id: emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                    }
                }
            }
        }

        // -- New 7TV global emotes:
        if (global_emotes.stv) {
            for (const emote of global_emotes.stv) {
                const _emote = await this.db.emotes.findFirst({
                    where: {
                        targetId: target.id,
                        id: emote.id,
                        provider: "stv",
                        is_global: true
                    }
                });

                if (!_emote) {
                    await this.db.emotes.create({
                        data: {
                            id: emote.id!,
                            name: emote.name!,
                            targetId: target.id,
                            provider: "stv",
                            is_global: true
                        }
                    });
                }
            }

            if (emotes) {
                for (const emote of emotes.filter(e => e.provider === "stv")) {
                    if (!emote.is_global) return;
                    const __emote: IEmote.STV | undefined = global_emotes.stv.find(e => e.id === emote.id);
                    
                    if (!__emote) {
                        await this.db.emotes.update({
                            where: {int_id: emote.int_id},
                            data: {
                                is_deleted: true
                            }
                        });
                    }
                }
            }
        }

        log.debug("Synchronized emotes with target ID", target.alias_id);
    }
}

export default EmoteUpdater;