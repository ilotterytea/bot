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

import IEmote from "emotelib/dist/interfaces/IEmote";
import { readdirSync, readFileSync, writeFileSync } from "fs";
import { Logger } from "tslog";
import TwitchApi from "../clients/ApiClient";
import IStorage from "../interfaces/IStorage";

const log: Logger = new Logger({name: "LocalStorage"});

class TargetStorage {
    private targets: {
        [target_id: string]: IStorage.Target
    }

    constructor(targets: {
        [target_id: string]: IStorage.Target
    }) {
        this.targets = targets;
    }

    public create(
        target_id: string,
        data: IStorage.Target
    ): void {
        if (this.containsTarget(target_id)) throw new Error(`Target with ID ${target_id} already exists in Targets.`);
        
        this.targets[target_id] = data;
    }

    public delete<T extends keyof IStorage.Target>(
        target_id: string,
        key?: T | undefined
    ): void {
        if (!this.containsTarget(target_id)) throw new Error(`Target with ID ${target_id} isn't in Targets.`);

        if (key === undefined) {
            delete this.targets[target_id];
            return;
        }

        if (!this.containsKey(target_id, key)) throw new Error(`Target with ID ${target_id} don't have key ${key}.`);

        delete this.targets[target_id][key];
    }

    public set<T extends keyof IStorage.Target>(
        target_id: string,
        key: T,
        value: IStorage.Target[T]
    ): void {
        if (!this.containsTarget(target_id)) throw new Error(`Target with ID ${target_id} isn't in Targets.`);
        
        this.targets[target_id][key] = value;
    }

    public get<T extends keyof IStorage.Target>(
        target_id: string,
        key?: T | undefined
    ): IStorage.Target | IStorage.Target[T] {
        if (!this.containsTarget(target_id)) throw new Error(`Target with ID ${target_id} isn't in Targets.`);
        
        if (key === undefined) {
            return this.targets[target_id];
        }

        if (!this.containsKey(target_id, key)) throw new Error(`Target with ID ${target_id} don't have key ${key}.`);

        return this.targets[target_id][key];
    }

    public containsTarget(target_id: string): boolean {
        if (!(target_id in this.targets)) return false;
        return true;
    }

    public containsKey<T extends keyof IStorage.Target>(
        target_id: string,
        key: T
    ): boolean {
        if (!(key in this.targets[target_id])) return false;
        return true;
    }

    get getTargets(): {[target_id: string]: IStorage.Target;} { return this.targets; }
}

class UserStorage {
    private data: {[user_id: string]: IStorage.User};

    constructor (data: IStorage.User[]) {
        this.data = {};
        this.parse(data);
    }

    public contains(user_id: string) { return user_id in this.data; }
    public containsValue(user_id: string, key: keyof IStorage.User) { return key in this.data[user_id]; }

    public set<T extends keyof IStorage.User>(user_id: string, key: T, value: IStorage.User[T]) : void {
        if (!this.contains(user_id)) throw new Error("User ID " + user_id + " not exists.");
        if (!this.containsValue(user_id, key)) throw new Error("No key " + key + " in user ID " + user_id);

        this.data[user_id][key] = value;
    }

    public get<T extends keyof IStorage.User>(user_id: string, key?: T | undefined) : IStorage.User[T] | IStorage.User | undefined {
        if (!(user_id in this.data)) return undefined;
        
        if (key === undefined) {
            return this.data[user_id];
        }

        return this.data[user_id][key];
    }

    public get getUsers() { return this.data; }

    private parse(data: IStorage.User[]): void {
        for (const dat of data) {
            this.data[dat.ID] = dat;
        }
    }
}

class GlobalStorage {
    private data: IStorage.Main;
    private symlinks: {
        [target_name: string]: string;
    }

    constructor (data: IStorage.Main) {
        this.data = data;
        this.symlinks = {};
    }

    public async convertIDsToUsernames(
        TwitchApi: TwitchApi.Client
    ) {
        this.symlinks = {};

        for await (const id of this.data.Join.AsClient) {
            const user = await TwitchApi.getUserById(id);
            if (user === undefined) {
                log.warn("User ID", id, "not found.");
                return;
            }

            this.symlinks[user.login] = id.toString();
            log.debug("Converted user ID", id, "to username", user.login);
        }
    }

    get getVersion() { return this.data.Version; }
    get getSymlinks() { return this.symlinks; }
    get getRawUsers() { return this.data.Global.Users; }
    get getModules() { return this.data.Global.Modules; }
    get getPrefix() { return this.data.Global.Prefix; }
    get getClientJoin() { return this.data.Join.AsClient; }
    get getGlobalData() { return this.data; }
}

/** @deprecated */
class LocalStorage {
    public Targets: TargetStorage;
    public Global: GlobalStorage;
    public Users: UserStorage;
    private paths: {
        targets: string,
        global: string
    };

    /**
     * Bot's local storage.
     * @param targets_folder Path to the folder with all channel files.
     * @param global_file Path to the global settings file.
     */
    constructor (targets_folder: string, global_file: string) {
        this.paths = {
            targets: targets_folder,
            global: global_file
        };

        this.Targets = new TargetStorage(
            this.loadTargets(targets_folder)
        );

        this.Global = new GlobalStorage(
            JSON.parse(readFileSync(global_file, {encoding: "utf-8"}))
        );

        this.Users = new UserStorage(this.Global.getRawUsers);
    }

    /**
     * Save the storage.
     * @param emotes Channel emotes.
     * @param timers Channel timers.
     */
    public save(
        emotes?: {[target_id: string]: {[provider_id: string]: IStorage.Emote[]}} | undefined,
        timers?: {[target_id: string]: {[timer_id: string]: IStorage.Timer}} | undefined,
        static_cmds?: {[target_id: string]: IStorage.Module[]} | undefined
    ) {
        var lines: number = 0;
        var characters: number = 0;
        var entries: number = 0;

        if (static_cmds !== undefined) {
            for (const id of Object.keys(static_cmds)) {
                this.Targets.set(id, "Modules", static_cmds[id]);
            }
        }

        if (emotes !== undefined) {
            for (const id of Object.keys(emotes)) {
                this.Targets.set(id, "Emotes", emotes[id]);
            }
        }
        
        if (timers !== undefined) {
            for (const id of Object.keys(timers)) {
                this.Targets.set(id, "Timers", timers[id]);
            }
        }

        for (const id of Object.keys(this.Users.getUsers)) {

        }

        // Saving channels to their files:
        for (const id of Object.keys(this.Targets.getTargets)) {
            lines = lines + JSON.stringify(this.Targets.get(id), null, 2).split('\n').length;
            characters = characters + JSON.stringify(this.Targets.get(id), null, 2).length;
            entries = entries + 1;

            writeFileSync(
                `${this.paths.targets}/${id}.json`,
                JSON.stringify(this.Targets.get(id), null, 2),
                {encoding: "utf-8"}
            );
        }

        // Saving the global file:
        writeFileSync(
            this.paths.global,
            JSON.stringify(this.Global.getGlobalData, null, 2),
            {encoding: "utf-8"}
        );

        lines = lines + JSON.stringify(this.Global.getGlobalData, null, 2).split('\n').length;
        characters = characters + JSON.stringify(this.Global.getGlobalData, null, 2).length;
        entries = entries + 1;

        log.debug("Saved", entries, "entries with a total of", lines, "lines and", characters, "characters.");
    }

    private loadTargets(targets_folder: string): {
        [target_id: string]: IStorage.Target
    } {
        var folder: string[] = readdirSync(targets_folder);
        var targets: {
            [target_id: string]: IStorage.Target
        } = {};

        for (const file of folder) {
            var content = JSON.parse(readFileSync(`${targets_folder}/${file}`, {encoding: "utf-8"}));

            targets[file.split('.')[0]] = content;
        }

        return targets;
    }
}

export default LocalStorage;