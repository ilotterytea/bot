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

import { readdirSync } from "fs";
import IArguments from "../interfaces/IArguments";
import IModule from "../interfaces/IModule";

// Commands:
import Ping from "../../shared_modules/Ping";

class ModuleManager {
    private modules: {[module_id: string]: IModule.IModule};

    constructor () {
        this.modules = {};
    }

    async call(module_id: string, args: IArguments, ...optional_args: any[]) : Promise<string | boolean> {
        if (!(module_id in this.modules)) return Promise.resolve(false);
        return Promise.resolve(await this.modules[module_id].run(args));
    }

    init() {
        this.modules["ping"] = new Ping(5000, "public", "public");
    }

    contains(module_id: string) {
        if (module_id in this.modules) return true;
        return false;
    }
}

export default ModuleManager;