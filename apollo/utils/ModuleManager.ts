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

import IArguments from "../interfaces/IArguments";
import IModule from "../interfaces/IModule";

// Commands:
import Ping from "../../shared_modules/Ping";
import Spam from "../../shared_modules/Spam";
import Massping from "../../shared_modules/Massping";
import EmoteCounter from "../../shared_modules/EmoteCounter";
import EmoteTop from "../../shared_modules/EmoteTop";
import SystemProcess from "../../shared_modules/SystemProcess";
import JoinChat from "../../shared_modules/JoinChat";
import Settings from "../../shared_modules/Settings";
import UserLookup from "../../shared_modules/UserData";
import TimerCreator from "../../shared_modules/TimerCreator";

class ModuleManager {
    private modules: {[module_id: string]: IModule.IModule};
    private cooldown: {[module_id: string]: string[]};

    constructor () {
        this.modules = {};
        this.cooldown = {};
    }

    async call(module_id: string, args: IArguments, ...optional_args: any[]) : Promise<string | boolean> {
        if (!(module_id in this.modules)) return Promise.resolve(false);

        this.createCooldownArray(module_id);

        if (this.cooldown[module_id].includes(args.user.id)) return Promise.resolve(false);

        if ((args.user.extRole < this.modules[module_id].permissions!)) {
            return Promise.resolve(false);
        }

        var response = Promise.resolve(await this.modules[module_id].run(args));
        this.cooldownUser(args.user.id, module_id, this.modules[module_id].cooldownMs!);

        return response;
    }

    init() {
        this.modules["ping"] = new Ping(5000, IModule.AccessLevels.PUBLIC);
        this.modules["spam"] = new Spam(30000, IModule.AccessLevels.MOD);
        this.modules["massping"] = new Massping(60000, IModule.AccessLevels.BROADCASTER);
        this.modules["ecount"] = new EmoteCounter(5000, IModule.AccessLevels.PUBLIC);
        this.modules["etop"] = new EmoteTop(10000, IModule.AccessLevels.PUBLIC);
        this.modules["system"] = new SystemProcess(0, IModule.AccessLevels.BROADCASTER);
        this.modules["join"] = new JoinChat(0, IModule.AccessLevels.PUBLIC);
        this.modules["set"] = new Settings(5000, IModule.AccessLevels.BROADCASTER);
        this.modules["user"] = new UserLookup(10000, IModule.AccessLevels.PUBLIC);
        this.modules["timer"] = new TimerCreator(10000, IModule.AccessLevels.BROADCASTER);
    }

    private createCooldownArray(module_id: string) {
        if (!(module_id in this.cooldown)) {
            this.cooldown[module_id] = [];
        }
        return true;
    }

    private putInCooldown(module_id: string, user_id: string) {
        this.createCooldownArray(module_id);
        if (this.cooldown[module_id].includes(user_id)) return false;

        this.cooldown[module_id].push(user_id);
        return true;
    }

    private cooldownUser(user_id: string, module_id: string, cooldownMs: number) {
        this.putInCooldown(module_id, user_id);

        setTimeout(() => {
            this.cooldown[module_id] = this.cooldown[module_id].filter(user => user !== user_id);
        }, cooldownMs);
    }

    contains(module_id: string) {
        if (module_id in this.modules) return true;
        return false;
    }

    get length() { return Object.keys(this.modules).length; }
}

export default ModuleManager;