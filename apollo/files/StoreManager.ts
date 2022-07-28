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

import STVProvider from "emotelib/dist/providers/STVProvider";
import { readdirSync, readFileSync, writeFileSync } from "fs";
import { Logger } from "tslog";
import TwitchApi from "../clients/ApiClient";
import IStorage from "../interfaces/IStorage";

const log: Logger = new Logger({name: "StoreManager"});

interface IManager<T> {
    add: (id: string | undefined, data?: T | undefined) => boolean | null;
    edit: (id: string | undefined, key: keyof T, value: any) => boolean | null;
    delete: (id: string | undefined) => boolean | null;
    get: (id: string | undefined, key?: keyof T | undefined) => any | null;

    isExists: (id: string | undefined) => boolean;
    isValueExists: (id: string | undefined, key: keyof T) => boolean | null;
}

class TargetManager {
    private data: {[target_id: string]: IStorage.Target};
    private g_data: IStorage.Main;
    private pchannel_func: any;

    constructor (target_data: {[target_id: string]: IStorage.Target}, global_data: IStorage.Main, parseChannels_function: any) {
        this.data = target_data;
        this.g_data = global_data;
        this.pchannel_func = parseChannels_function;
    }

    async add(target_id: string | undefined, data?: IStorage.Target | undefined) {
        if (this.isExists(target_id)) return false;
        if (target_id === undefined) return false;
        if (data === undefined) {
            data = {
                Name: "",
                SuccessfullyCompletedTests: 0,
                ExecutedCommands: 0,
                ChatLines: 0,
                Emotes: {},
                Modules: {}
            }
        }

        this.data[target_id] = data;
        this.g_data.Join.AsClient.push(parseInt(target_id));
        await this.pchannel_func(this.g_data.Join.AsClient);
        return true;
    }
    edit(target_id: string | undefined, key: keyof IStorage.Target, value?: any) {
        if (target_id === undefined) return false;
        if (!this.isExists(target_id)) return false;

        if (value === undefined) {
            delete this.data[target_id][key];
            return true;
        }
        
        this.data[target_id][key] = value;
        return true;
    }
    delete(target_id: string | undefined) {
        if (target_id === undefined) return false;
        if (!this.isExists(target_id)) return false;
        delete this.data[target_id];
        return true;
    }
    get(target_id: string | undefined, key?: keyof IStorage.Target | undefined) {
        if (target_id === undefined) return false;
        if (!this.isExists(target_id)) return false;

        if (key === undefined) {
            return this.data[target_id];
        }

        if (!this.isValueExists(target_id, key)) return false;

        return this.data[target_id][key];
    }

    isExists(target_id: string | undefined) {
        if (target_id === undefined) return false;
        if (!(target_id in this.data)) return false;
        return true;
    }

    isValueExists(target_id: string | undefined, key: keyof IStorage.Target) {
        if (!this.isExists(target_id)) return false;
        if (target_id === undefined) return false;
        if (!(key in this.data[target_id])) return false;
        return true;
    }

    getUserlinks() {
        var users: {[user_name: string]: string} = {};

        Object.keys(this.data).forEach((user) => {
            users[this.data[user].Name!] = user;
        });

        return users;
    }
    get getTargets() { return this.data; }
}

class UserManager implements IManager<IStorage.User> {
    private data: {[user_id: string]: IStorage.User};

    constructor (user_data: {[user_id: string]: IStorage.User}) {
        this.data = user_data;
    }

    add(id: string | undefined, data?: IStorage.User | undefined) {
        if (this.isExists(id)) return false;
        if (id === undefined) return false;
        if (data === undefined) {
            data = {
                InternalType: ""
            }
        }
        this.data[id] = data;
        return true;
    }
    edit(id: string | undefined, key: keyof IStorage.User, value: any) {
        if (!this.isExists(id)) return false;
        if (!this.isValueExists(id, key)) return false;
        this.data[id!][key] = value;
        return true;
    }
    delete(id: string | undefined) {
        if (id === undefined) return false;
        if (!this.isExists(id)) return false;
        delete this.data[id];
        return true;
    }
    get(id: string | undefined, key?: keyof IStorage.User | undefined) {
        if (!this.isExists(id)) return false;

        if (key === undefined) {
            return this.data[id!];
        }

        if (!this.isValueExists(id, key)) return false;

        return this.data[id!][key];
    }
    isExists(id: string | undefined) {
        if (id === undefined) return false;
        if (!(id in this.data)) return false;
        return true;
    }
    isValueExists(id: string | undefined, key: keyof IStorage.User) {
        if (id === undefined) return false;
        if (!this.isExists(id)) return false;
        if (!(key in this.data[id])) return false;
        return true;
    }
}

class StoreManager {
    private global_data: IStorage.Main;
    private target_data: {[target_id: string]: IStorage.Target};
    private file_paths: {[path_id: string]: string};
    private ApiClient: TwitchApi.Client;
    private target_names: string[];

    targets: TargetManager;
    users: UserManager;

    constructor (global_file_path: string, target_folder_path: string, twitch_api: TwitchApi.Client) {
        this.ApiClient = twitch_api;
        this.target_names = [];

        this.file_paths = {
            global: global_file_path,
            target: target_folder_path
        };

        this.global_data = JSON.parse(readFileSync(this.file_paths["global"], {encoding: "utf-8"}));
        this.target_data = this.multiDictLoad(this.file_paths["target"]);
        this.targets = new TargetManager(this.target_data, this.global_data, this.parseChannels);
        this.users = new UserManager(this.global_data.Global.Users!);
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

    async parseChannels(target_ids: number[]) {
        
        target_ids.forEach(async (id) => {
            await this.ApiClient.getUserById(id).then((user) => {
                if (user === undefined) return false;
                this.target_names.push(user.login);
            });
        });
    }
    
    // Save all data:
    async save(stv_emotes: {[target_name: string]: {[emote_name: string]: IStorage.Emote}}) {
        var entries: number = 0;
        var lines: number = 0;

        Object.keys(stv_emotes).forEach((e_target) => {
            Object.keys(this.target_data).forEach((target) => {
                if (this.target_data[target].Name! === e_target) {
                    this.target_data[target].Emotes!["stv"] = stv_emotes[e_target];
                }
            });
        });

        lines = lines + JSON.stringify(this.global_data, null, 2).split('\n').length;

        writeFileSync(this.file_paths["global"], JSON.stringify(this.global_data, null, 2), {encoding: "utf-8"});
        entries++;

        Object.keys(this.target_data).forEach((target) => {
            lines = lines + JSON.stringify(this.target_data[target], null, 2).split('\n').length;
            writeFileSync(`${this.file_paths["target"]}/${target}.json`, JSON.stringify(this.target_data[target], null, 2), {encoding: "utf-8"});
            entries++;
        });
        log.debug("Saved", entries, "entries with", lines, "lines total!");
    }

    // Global manipulations:
    get getVersion() { return this.global_data.Version; }
    get getClientChannelIDs() { return this.global_data.Join?.AsClient; }
    get getClientChannelNames() : string[] { return this.target_names; }
    get getAnonymousChannelIDs() { return this.global_data.Join?.AsAnonymous; }
    get getGlobalPrefix() { return this.global_data.Global.Prefix; }
    get getGlobalModules() { return this.global_data.Global.Modules; }
}

export default StoreManager;