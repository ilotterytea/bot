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

export default class Ping implements IModule.IModule {
    cooldownMs: number;
    permissions: number;
    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {
        var totalmem = await (await os.mem.used()).totalMemMb;
        var usedmem = await (await os.mem.used()).usedMemMb;

        var pingms = Math.floor(Math.round((await Arguments.Services.Client.ping())[0] * 1000));
        
        return Promise.resolve(Arguments.Services.Locale.parsedText("cmd.ping.response", Arguments, [
            usedmem.toString(),
            totalmem.toString(),
            pingms.toString()
        ]));
    }
}