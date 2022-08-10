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

import { Emotes, Target } from "@prisma/client";
import IArguments from "../apollo/interfaces/IArguments";
import IModule from "../apollo/interfaces/IModule";
import IStorage from "../apollo/interfaces/IStorage";

export default class EmoteCounter implements IModule.IModule {
    cooldownMs: number;
    permissions: number;

    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {
        const emote: string = Arguments.Message.raw.split(' ')[1];

        const target: Target | null = await Arguments.Services.DB.target.findFirst({
            where: {alias_id: parseInt(Arguments.Target.ID)}
        });

        if (!target) return Promise.resolve(false);

        const stv_emote: Emotes | null = await Arguments.Services.DB.emotes.findFirst({
            where: {
                name: emote,
                targetId: target.id,
                provider: "stv"
            }
        });

        const bttv_emote: Emotes | null = await Arguments.Services.DB.emotes.findFirst({
            where: {
                name: emote,
                targetId: target.id,
                provider: "bttv"
            }
        });

        const ffz_emote: Emotes | null = await Arguments.Services.DB.emotes.findFirst({
            where: {
                name: emote,
                targetId: target.id,
                provider: "ffz"
            }
        });

        const ttv_emote: Emotes | null = await Arguments.Services.DB.emotes.findFirst({
            where: {
                name: emote,
                targetId: target.id,
                provider: "ttv"
            }
        });

        if (stv_emote) {
            return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.ecount.response", Arguments, [
                "[7TV]",
                emote,
                stv_emote.used_times
            ]));
        }
        if (bttv_emote) {
            return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.ecount.response", Arguments, [
                "[BTTV]",
                emote,
                bttv_emote.used_times
            ]));
        }
        if (ffz_emote) {
            return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.ecount.response", Arguments, [
                "[FFZ]",
                emote,
                ffz_emote.used_times
            ]));
        }
        if (ttv_emote) {
            return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.ecount.response", Arguments, [
                "[Twitch]",
                emote,
                ttv_emote.used_times
            ]));
        }
        return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.ecount.not_found", Arguments, [
            "",
            emote
        ]));
    }
}