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

export default class Massping implements IModule.IModule {
    cooldownMs: number;
    permissions: IModule.AccessLevels;
    minPermissions?: IModule.AccessLevels;

    constructor (cooldownMs: number, perms: IModule.AccessLevels, minperms?: IModule.AccessLevels | undefined) {
        this.cooldownMs = cooldownMs;
        this.permissions = perms;
        this.minPermissions = minperms;
    }

    async run(Arguments: IArguments) {
        const response = await axios.get(`https://tmi.twitch.tv/group/user/${Arguments.target.name.slice(1, Arguments.target.name.length)}/chatters`);
        const chatters: {[role_name: string]: string[]} = response.data.chatters;
        const message: string[] = Arguments.message.raw!.split(' ');

        delete message[0];

        for (var i = 0; i < chatters.vips.length; i++) {
            Arguments.client.say(Arguments.target.name, `@${chatters.vips[i]}, ${message.join(' ').trim()}`);
        }

        for (var i = 0; i < chatters.moderators.length; i++) {
            Arguments.client.say(Arguments.target.name, `@${chatters.moderators[i]}, ${message.join(' ').trim()}`);
        }

        for (var i = 0; i < chatters.staff.length; i++) {
            Arguments.client.say(Arguments.target.name, `@${chatters.staff[i]}, ${message.join(' ').trim()}`);
        }

        for (var i = 0; i < chatters.admins.length; i++) {
            Arguments.client.say(Arguments.target.name, `@${chatters.admins[i]}, ${message.join(' ').trim()}`);
        }

        for (var i = 0; i < chatters.global_mods.length; i++) {
            Arguments.client.say(Arguments.target.name, `@${chatters.global_mods[i]}, ${message.join(' ').trim()}`);
        }

        for (var i = 0; i < chatters.viewers.length; i++) {
            Arguments.client.say(Arguments.target.name, `@${chatters.viewers[i]}, ${message.join(' ').trim()}`);
        }

        return Promise.resolve(true);
    }
}