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

import { PrismaClient, Timers } from "@prisma/client";
import { Client } from "tmi.js";
import { Logger } from "tslog";
import IArguments from "../interfaces/IArguments";

const log: Logger = new Logger({name: "TimerHandler"});

class TimerHandler {
    private symlink: {[target_id: string]: string};
    private ticks: {[target_id: string]: {[timer_id: string]: NodeJS.Timer}};
    private db: PrismaClient;

    constructor (db: PrismaClient) {
        this.symlink = {};
        this.ticks = {};
        this.db = db;        
    }

    /**
     * Initialize the timer handler.
     * @param symlink Assigned user names to their user IDs.
     */
    public async init(symlink: {[target_name: string]: string}) {
        for (const name of Object.keys(symlink)) {
            this.symlink[symlink[name]] = name;
        }

        log.debug("Reversed the symlinks for TimerHandler (target_id: target_name).");
        log.debug("Timer Handler is ready for ticking!");
    }

    public async tick(client: Client) {
        for await (const t_id of await this.db.target.findMany({
            select: {timers: true, alias_id: true}
        })) {
            // Destroy the existing ticking timers:
            if (t_id.alias_id in this.ticks) {
                for (const timer of Object.keys(this.ticks[t_id.alias_id])) {
                    clearInterval(this.ticks[t_id.alias_id][timer]);
                }
            }

            this.ticks[t_id.alias_id] = {};

            for (const timer of t_id.timers) {
                if (!(t_id.alias_id in this.symlink)) return;

                if (timer.value == false) return;

                this.ticks[t_id.alias_id][timer.id] = setInterval(() => {
                    client.say(`#${this.symlink[t_id.alias_id]}`, timer.response);
                }, timer.interval_ms);
            }
        }
    }

    public disposeTick(target_id: string, timer_id: string): boolean {
        if (!(target_id in this.ticks)) return false;
        if (!(timer_id in this.ticks[target_id])) return false;

        clearInterval(this.ticks[target_id][timer_id]);
        delete this.ticks[target_id][timer_id];

        return true;
    }

    public newTick(
        target_id: string,
        timer_id: string,
        client: Client,
        target_name: string,
        response: string,
        intervalMs: number
        ): void {
        if (!(target_id in this.ticks)) this.ticks[target_id] = {};
        
        this.disposeTick(target_id, timer_id);

        this.ticks[target_id][timer_id] = setInterval(() => {
            client.say(`#${target_name}`, response);
        }, intervalMs);
    }

    public containsTick(target_id: string, timer_id: string): boolean { return timer_id in this.ticks[target_id]; }

    public get getTicks() { return this.ticks; }
}

export default TimerHandler;