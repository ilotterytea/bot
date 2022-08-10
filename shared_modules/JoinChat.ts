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
import os from "node-os-utils";
import { readFileSync } from "fs";
import { short, branch } from "git-rev-sync";
import packagejson from "../package.json";
import { client } from "tmi.js";
import IStorage from "../apollo/interfaces/IStorage";

export default class JoinChat implements IModule.IModule {
    cooldownMs: number;
    permissions: number;
    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {
        if (!Arguments.Services.TwitchApi) return Promise.resolve(false);

        const _message: string[] = Arguments.Message.raw.split(' ');
        const channel: string = (_message[1] && Arguments.Sender.intRole == IStorage.InternalRoles.SUPAUSER) ? _message[1] : Arguments.Sender.Username;
        const user = await Arguments.Services.TwitchApi.getUserByName(channel);

        if (!user) return Promise.resolve(false);

        await Arguments.Services.DB.target.create({
            data: {
                alias_id: parseInt(user.id)
            }
        });

        await Arguments.Services.Symlinks.register(user.id);

        await Arguments.Services.Client.join(`#${user.login}`);

        if (Arguments.Services.Emote) await Arguments.Services.Emote.syncAllEmotes(user.id);

        return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.join.response", Arguments, [
            user.login,
            user.id
        ]));
    }
}