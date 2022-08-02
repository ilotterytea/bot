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
        const emote: string = Arguments.message.raw!.split(' ')[1];

        if (!(emote in Arguments.channel_emotes!)) {
            return Promise.resolve(Arguments.localizator!.parsedText("cmd.ecount.not_found", Arguments, ["[7TV]", emote]));
        }

        const usedtimes: number = Arguments.channel_emotes![emote].UsedTimes;

        return Promise.resolve(Arguments.localizator!.parsedText("cmd.ecount.response", Arguments, ["[7TV]", emote, usedtimes.toString()]));
    }
}