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

import IStorage from "../interfaces/IStorage";

class StaticCommandHandler {
    private data: {
        [target_id: string]: IStorage.Module[]
    }

    constructor (data: {[target_id: string]: IStorage.Target}) {
        this.data = {}
        this.load(data);
    }

    public create(target_id: string, data: IStorage.Module) {
        if (!this.contains(target_id)) this.data[target_id] = [];
        if (this.containsCmd(target_id, data.ID) !== undefined) throw new Error("");
        this.data[target_id].push(data);
    }

    public remove(target_id: string, static_id: string) {
        if (!this.contains(target_id)) throw new Error("");

        this.data[target_id] = this.data[target_id].filter(e => e.ID !== static_id);
    }

    public set<T extends keyof IStorage.Module>(
        target_id: string,
        static_id: string,
        key: T,
        value: IStorage.Module[T]
    ) {
        if (!this.contains(target_id)) throw new Error("");
        if (this.containsCmd(target_id, static_id) === undefined) throw new Error("");

        var _data = this.data[target_id].find(m => m.ID === static_id);

        if (_data === undefined) throw new Error("");

        _data[key] = value;
    }

    public get<T extends keyof IStorage.Module>(
        target_id: string,
        static_id: string
    ) {
        if (!this.contains(target_id)) throw new Error("");
        if (this.containsCmd(target_id, static_id) === undefined) return undefined;

        const _data = this.data[target_id].find(m => m.ID === static_id);

        return _data;
    }

    public disable(target_id: string, static_id: string) {
        if (!this.contains(target_id)) throw new Error("");
        if (this.containsCmd(target_id, static_id) === undefined) throw new Error("");

        var _data = this.data[target_id].find(e => e.ID === static_id);
        if (_data === undefined) throw new Error("");

        _data.Value = false;
    }

    public enable(target_id: string, static_id: string) {
        if (!this.contains(target_id)) throw new Error("");
        if (this.containsCmd(target_id, static_id) === undefined) throw new Error("");

        var _data = this.data[target_id].find(e => e.ID === static_id);
        if (_data === undefined) throw new Error("");

        _data.Value = true;
    }

    public get getCmds() { return this.data; }

    public containsCmd(target_id: string, static_id: string) { return this.data[target_id].find(e=>e.ID===static_id); }
    public contains(target_id: string) { return target_id in this.data; }

    private load(data: {[target_id: string]: IStorage.Target}) {
        for (const id of Object.keys(data)) {
            if (data[id].Modules === undefined) return;
            this.data[id] = data[id].Modules!;
        }
    }
}

export default StaticCommandHandler;