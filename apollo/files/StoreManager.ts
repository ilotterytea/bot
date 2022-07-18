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

import { readFileSync, writeFileSync } from "fs";
import { Logger } from "tslog";
import IStorage from "../interfaces/IStorage";

const log: Logger = new Logger({name: "StoreManager"});

class StoreManager {
    private data: IStorage.Main;
    private file_path: string;

    constructor (file_path: string) {
        this.file_path = file_path;
        this.data = JSON.parse(readFileSync(this.file_path, {encoding: "utf-8"}));

        log.debug("Data storage successfully loaded from file ", this.file_path);
    }

    async save() {
        writeFileSync(
            this.file_path,
            JSON.stringify(this.data, null, 2),
            {
                encoding: "utf-8"
            }
        );
    }

    removeTarget(target_id: string) {
        if (!(this.containsTarget(target_id))) return false;
        delete this.data.Targets[target_id];
        return true;
    }

    addTarget(target_id: string, values?: IStorage.Target | undefined) {
        if (this.containsTarget(target_id)) return false;

        if (values === undefined) {
            this.data.Targets[target_id] = {};
            return true;
        } else {
            this.data.Targets[target_id] = values;
        }

        return true;
    }

    updateTarget(target_id: string, key: keyof IStorage.Target, value: any) {
        if (!(this.containsTarget(target_id))) {
            this.addTarget(target_id);
        }

        this.data.Targets[""][key] = value;

        return true;
    }

    containsTarget(target_id: string) {
        if (!(target_id in this.data.Targets)) return false;
        return true;
    }

    get getClientJoinID() {
        return this.data.Join?.AsClient;
    }

    get getAnonymousJoinIDs() {
        return this.data.Join?.AsAnonymous;
    }

    get getFullData() {
        return this.data;
    }

    getPrefix(target_id: string) : string {
        if (Object.keys(this.data.Targets[target_id]).includes("Prefix")) return this.data.Targets[target_id].Prefix!;
        return this.data.Global.Prefix;
    }
}

export default StoreManager;