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

class Ping implements IModule.IModule {
    cooldownMs: number;
    permissions: IModule.Permissions;
    minPermissions: IModule.Permissions;

    constructor (cooldownMs: number, perms: IModule.Permissions, minperms: IModule.Permissions) {
        this.cooldownMs = cooldownMs;
        this.permissions = perms;
        this.minPermissions = minperms;
    }

    async run(Arguments: IArguments) {
        var totalmem = await (await os.mem.used()).totalMemMb;
        var usedmem = await (await os.mem.used()).usedMemMb;
        
        function formatTime(seconds: number) {
            function pad(s: number) {
                return (s < 10 ? '0' : '') + s.toString();
            }

            var days = Math.floor(seconds / (60 * 60 * 24));
            var hours = Math.floor(seconds / (60 * 60) % 24);
            var minutes = Math.floor(seconds % (60 * 60) / 60);
            var sec = Math.floor(seconds % 60);

            return `${days} d. ${pad(hours)}:${pad(minutes)}:${pad(sec)}`;
        }

        var pingms = Math.floor(Math.round((await Arguments.client.ping())[0] * 1000));
        var uptime = formatTime(process.uptime());
        var channels = Arguments.storage.getClientJoinID?.length;
                
        return Promise.resolve(Arguments.localizator.parsedText("cmd.ping.exec.response", Arguments.target.id, "FeelsDankMan  🏓 ", uptime, channels, `${usedmem} ${Arguments.localizator.parsedText("measure.megabyte", Arguments.target.id)}/${totalmem} ${Arguments.localizator.parsedText("measure.megabyte", Arguments.target.id)}`, pingms, `${packagejson.version}-${packagejson.name}`, short(), branch()));
    }
}

export default Ping;