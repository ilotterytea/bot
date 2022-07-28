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
import child from "child_process";

export default class SystemProcess implements IModule.IModule {
    cooldownMs: number;
    permissions: IModule.AccessLevels;
    minPermissions?: IModule.AccessLevels;

    constructor (cooldownMs: number, perms: IModule.AccessLevels, minperms?: IModule.AccessLevels | undefined) {
        this.cooldownMs = cooldownMs;
        this.permissions = perms;
        this.minPermissions = minperms;
    }

    async run(Arguments: IArguments) {
        const turi_ip_ip = Arguments.message.raw!.split(' ')[1];

        switch (turi_ip_ip) {
            case "restart":
                await Arguments.client.say(Arguments.target.name, "forsenSpin restarting...");
                await Arguments.storage.save(Arguments.stv!.getEmotes);
                child.exec("git pull");
                child.execSync("ts-node index.ts --debug");
                break;
            case "poweroff":
                await Arguments.client.say(Arguments.target.name, "forsenLevel 👋 bye bye");
                await Arguments.storage.save(Arguments.stv!.getEmotes);
                process.exit(0);
            case "machine":
                const xd = Arguments.message.raw!.split(' ')[2];

                if (xd == "restart") {
                    await Arguments.client.say(Arguments.target.name, "forsenSpin 💻 restarting the machine...");
                    await Arguments.storage.save(Arguments.stv!.getEmotes);
                    child.exec("shutdown -r now");
                }

                if (xd == "shutdown") {
                    await Arguments.client.say(Arguments.target.name, "forsenLevel 👋 💻 bye bye machine and you");
                    await Arguments.storage.save(Arguments.stv!.getEmotes);
                    child.exec("shutdown now");
                }
                break;
            default:
                break;
        }

        return Promise.resolve(true);
    }
}