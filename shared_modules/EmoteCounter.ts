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

import IArguments from "../apollo/interfaces/IArguments";
import IModule from "../apollo/interfaces/IModule";
import IStorage from "../apollo/interfaces/IStorage";

export default class EmoteCounter implements IModule.IModule {
    cooldownMs: number;
    permissions: IModule.AccessLevels;
    minPermissions?: IModule.AccessLevels;

    constructor (cooldownMs: number, perms: IModule.AccessLevels, minperms?: IModule.AccessLevels | undefined) {
        this.cooldownMs = cooldownMs;
        this.permissions = perms;
        this.minPermissions = minperms;
    }

    async run(Arguments: IArguments) {
        if (Arguments.Target.Emotes === undefined) return Promise.resolve(false);
        const emote: string = Arguments.Message.raw.split(' ')[1];
        const stv_emotes: IStorage.Emote | undefined = Arguments.Target.Emotes["stv"].find(e => e.Name === emote);
        const bttv_emotes: IStorage.Emote | undefined = Arguments.Target.Emotes["bttv"].find(e => e.Name === emote);
        const ffz_emotes: IStorage.Emote | undefined = Arguments.Target.Emotes["ffz"].find(e => e.Name === emote);
        const ttv_emotes: IStorage.Emote | undefined = Arguments.Target.Emotes["ttv"].find(e => e.Name === emote);

        if (stv_emotes !== undefined) {
            return Promise.resolve(Arguments.Services.Locale.parsedText("cmd.ecount.response", Arguments, [
                "[7TV]",
                emote,
                stv_emotes.UsedTimes
            ]));
        }
        if (bttv_emotes !== undefined) {
            return Promise.resolve(Arguments.Services.Locale.parsedText("cmd.ecount.response", Arguments, [
                "[BTTV]",
                emote,
                bttv_emotes.UsedTimes
            ]));
        }
        if (ffz_emotes !== undefined) {
            return Promise.resolve(Arguments.Services.Locale.parsedText("cmd.ecount.response", Arguments, [
                "[FFZ]",
                emote,
                ffz_emotes.UsedTimes
            ]));
        }
        if (ttv_emotes !== undefined) {
            return Promise.resolve(Arguments.Services.Locale.parsedText("cmd.ecount.response", Arguments, [
                "[Twitch]",
                emote,
                ttv_emotes.UsedTimes
            ]));
        }
        return Promise.resolve(true);
    }
}