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
import axios from "axios";

export default class UserLookup implements IModule.IModule {
    cooldownMs: number;
    permissions: number;
    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {
        var _message: string[] = Arguments.Message.raw.split(' ');
        var username: string = _message[1];

        var onlyPfp: boolean = _message.includes('--pic') || _message.includes('-p');
        var onlyRules: boolean = _message.includes('--rules') || _message.includes('-r');

        var response = await axios.get("https://api.ivr.fi/twitch/resolve/" + username, {responseType: "json"});

        const data = response.data;

        if (response.status != 200) return Promise.resolve(await Arguments.Services.Locale.parsedText("msg.api_error", Arguments, [
            `${response.status}`
        ]));

        if (onlyRules) {
            return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.user.rules", Arguments, [
                data.login,
                data.id,
                data.chatSettings.rules.join(', ')
            ]));
        }

        if (onlyPfp) {
            return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.user.picture", Arguments, [
                data.login,
                data.id,
                data.logo
            ]));
        }

        return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.user.lookup", Arguments, [
            data.displayName,
            data.login,
            data.id,
            data.bio,
            data.chatColor,
            (data.partner) ? "✅" : "⛔",
            (data.affiliate) ? "✅" : "⛔",
            (data.bot) ? "✅" : "⛔",
            (data.banned) ? "✅" : "⛔"
        ]));
    }
}