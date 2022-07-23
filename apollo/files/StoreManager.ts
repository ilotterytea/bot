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

import { readdirSync, readFileSync, writeFileSync } from "fs";
import { Logger } from "tslog";
import IStorage from "../interfaces/IStorage";

const log: Logger = new Logger({name: "StoreManager"});

interface IManager<T> {
    add: (id: string, data?: T | undefined) => boolean | null;
    edit: (id: string, key: keyof T, value: any) => boolean | null;
    delete: (id: string) => boolean | null;
    get: (id: string, key?: keyof T | undefined) => any | null;

    isExists: (id: string) => boolean;
    isValueExists: (id: string, key: keyof T) => boolean | null;
}

class TargetManager implements IManager<IStorage.Target> {
    private data: {[target_id: string]: IStorage.Target};

    constructor (target_data: {[target_id: string]: IStorage.Target}) {
        this.data = target_data;
    }

    add(target_id: string, data?: IStorage.Target | undefined) {
        return true;
    }
    edit(target_id: string, key: keyof IStorage.Target, value: any) {
        return true;
    }
    delete(target_id: string, data?: IStorage.Target | undefined) {
        return true;
    }
    get(target_id: string, key?: keyof IStorage.Target | undefined) {
        return true;
    }

    isExists(target_id: string) {
        return true;
    }

    isValueExists(target_id: string, key: keyof IStorage.Target) {
        return true;
    }
}

class StoreManager {
    private global_data: IStorage.Main;
    private target_data: {[target_id: string]: IStorage.Target};
    private file_paths: {[path_id: string]: string};
    targets: TargetManager;

    constructor (global_file_path: string, target_folder_path: string) {
        this.file_paths = {
            global: global_file_path,
            target: target_folder_path
        };


        this.global_data = JSON.parse(readFileSync(this.file_paths["global"], {encoding: "utf-8"}));
        this.target_data = this.multiDictLoad(this.file_paths["target"]);
        this.targets = new TargetManager(this.target_data);
    }

    private multiDictLoad(folder_path: string) {
        var files = readdirSync(folder_path);
        var dict: {[file_name: string]: any} = {};

        files.forEach((file) => {
            var file_name: string = file.split('.')[0];
            var file_data: any = JSON.parse(readFileSync(`${folder_path}/${file}`, {encoding: "utf-8"}));

            if (!(file_name in dict)) dict[file_name] = {};

            dict[file_name] = file_data;
        });

        return dict;
    }
    
    // Save all data:
    save() {
        var entries: number = 0;

        writeFileSync(this.file_paths["global"], JSON.stringify(this.global_data, null, 2), {encoding: "utf-8"});
        entries++;

        Object.keys(this.target_data).forEach((target) => {
            writeFileSync(`${this.file_paths["target"]}/${target}.json`, JSON.stringify(this.target_data[target], null, 2), {encoding: "utf-8"});
            entries++;
        });
        log.debug("Saved", entries, "entries!");
    }

    // Channel manipulations:
    createTarget(target_id: string, data: IStorage.Target) {
        if (this.containsTarget(target_id)) return false;

        this.target_data[target_id] = {};
        var target: IStorage.Target = data;
        this.target_data[target_id] = {};

        return true;
    }
    removeTarget(target_id: string) {
        if (!this.containsTarget(target_id)) return false;
        delete this.target_data[target_id];
        return true;
    }
    changeTargetValue(target_id: string, key: keyof IStorage.Target, value: any) {
        if (!this.containsTarget(target_id)) return false;

        this.target_data[target_id][key] = value;
        return true;
    }
    getTargetValue(target_id: string, key: keyof IStorage.Target) {
        if (!this.containsTarget(target_id)) return false;
        return this.target_data[target_id][key];
    }
    getTarget(target_id: string) {
        if (!this.containsTarget(target_id)) return false;
        return this.target_data[target_id];
    }
    containsTarget(target_id: string) {
        if (!(target_id in this.target_data)) return false;
        return true;
    }
    containsTargetValue(target_id: string, key: keyof IStorage.Target) {
        if (!this.containsTarget(target_id)) return false;
        if (!(key in this.target_data[target_id])) return false;
        return true;
    }
    get getTargets() { return this.target_data; }

    // Global manipulations:
    get getVersion() { return this.global_data.Version; }
    get getClientJoin() { return this.global_data.Join?.AsClient; }
    get getAnonymousJoin() { return this.global_data.Join?.AsAnonymous; }
    get getGlobalPrefix() { return this.global_data.Global.Prefix; }
    get getGlobalModules() { return this.global_data.Global.Modules; }


}

export default StoreManager;