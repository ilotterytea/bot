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

import { Client } from "tmi.js";
import TwitchApi from "../clients/ApiClient";
import IArguments from "../interfaces/IArguments";
import IStorage from "../interfaces/IStorage";

class TimerHandler {
    private timers: {[target_id: string]: {[TimerName: string]: IStorage.Timer}};
    private symlink: {[target_id: string]: string};
    private tickDict: {[target_id: string]: {[timer_id: string]: NodeJS.Timer}};

    constructor (targets: {[target_id: string]: IStorage.Target}) {
        this.timers = {};
        this.symlink = {};
        this.tickDict = {};

        for (const target of Object.keys(targets)) {
            this.timers[target] = targets[target].Timers!;
        }
    }

    public async IDsToUsernames(ttvApi: TwitchApi.Client) {
        for await (const t_id of Object.keys(this.timers)) {
            const user = await ttvApi.getUserById(parseInt(t_id));
            
            if (user === undefined) return false;

            this.symlink[t_id] = user.login;
        }
    }

    public tick(client: Client) {
        for (const t_id of Object.keys(this.timers)) {
            this.tickDict[t_id] = {};

            for (const timer of Object.keys(this.timers[t_id])) {
                if (!(t_id in this.symlink)) return false;

                if (this.timers[t_id][timer].Value == false) return false;
                if (this.timers[t_id][timer].Response === undefined) return false;

                this.tickDict[t_id][timer] = setInterval(() => {
                    this.timers[t_id][timer].Response.forEach((msg) => {
                        client.say(`#${this.symlink[t_id]}`, msg);
                    });
                }, this.timers[t_id][timer].IntervalMs);
            }
        }
    }

    public async reload(client: Client, ttv_api: TwitchApi.Client, targets: {[target_id: string]: IStorage.Target}) {
        this.timers = {};
        this.symlink = {};

        // Removing the existing intervals:
        for (const target of Object.keys(this.tickDict)) {
            for (const timer of Object.keys(this.tickDict[target])) {
                this.disposeTick(target, timer);
            }
        }

        for (const target of Object.keys(targets)) {
            this.timers[target] = targets[target].Timers!;
        }

        await this.IDsToUsernames(ttv_api);

        this.tick(client);

        return true;
    }

    public disposeTick(target_id: string, timer_id: string) {
        if (!(target_id in this.tickDict)) return false;
        if (!(timer_id in this.tickDict[target_id])) return false;

        clearInterval(this.tickDict[target_id][timer_id]);
        delete this.tickDict[target_id][timer_id];

        return true;
    }

    public createTimer(target_id: string, timer_id: string, data: IStorage.Timer, client: Client) {
        if (timer_id in this.timers[target_id]) return false;

        this.timers[target_id][timer_id] = data;

        if (data.Value) this.newTick(target_id, timer_id, client, this.symlink[target_id], data.Response, data.IntervalMs);

        return true;
    }

    public disableTimer(target_id: string, timer_id: string) {
        if (!(target_id in this.tickDict)) return false;
        if (!(timer_id in this.tickDict[target_id])) return false;

        this.timers[target_id][timer_id].Value = false;
        this.disposeTick(target_id, timer_id);
        return true;
    }

    public enableTimer(target_id: string, timer_id: string, args: IArguments) {
        if (!(target_id in this.tickDict)) return this.tickDict[target_id] = {};
        if (timer_id in this.tickDict[target_id]) return false;

        this.timers[target_id][timer_id].Value = true;
        this.newTick(target_id, timer_id, args.Services.Client, args.Target.Username, this.timers[target_id][timer_id].Response, this.timers[target_id][timer_id].IntervalMs);
        return true;
    }

    public getTimer(target_id: string, timer_id: string) {
        if (!(target_id in this.timers)) return false;
        if (!(timer_id in this.timers[target_id])) return false;

        return this.timers[target_id][timer_id];
    }

    public removeTimer(target_id: string, timer_id: string) {
        delete this.timers[target_id][timer_id];
        this.disposeTick(target_id, timer_id);

        return true;
    }

    public newTick(
        target_id: string,
        timer_id: string,
        client: Client,
        target_name: string,
        responses: string[],
        intervalMs: number
        ) {

        this.tickDict[target_id][timer_id] = setInterval(() => {
            for (const msg of responses) {
                client.say(`#${target_name}`, msg);
            }
        }, intervalMs);
    }

    public get getTicks() { return this.tickDict; }
    public get getTimers() { return this.timers; }
}

export default TimerHandler;