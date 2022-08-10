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

import { Target, Timers } from "@prisma/client";
import IArguments from "../apollo/interfaces/IArguments";
import IModule from "../apollo/interfaces/IModule";

export default class TimerCreator implements IModule.IModule {
    cooldownMs: number;
    permissions: number;
    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {
        if (Arguments.Services.Timer === undefined) return Promise.resolve(false);
        const target: Target | null = await Arguments.Services.DB.target.findFirst({
            where: {alias_id: parseInt(Arguments.Target.ID)}
        });

        if (!target) return Promise.resolve(false);

        const _message: string[] = Arguments.Message.raw.split(' ');
        const option: string = _message[1];
        const timer_id: string = _message[2];
        const intervalsec: number = parseInt(_message[3]) * 1000;

        delete _message[0];
        delete _message[1];
        delete _message[2];
        delete _message[3];
        
        switch (option) {
            case "--new": {
                if (isNaN(intervalsec)) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("timer.incorrect_interval", Arguments, [
                        timer_id,
                        intervalsec
                    ]));
                }
                
                const msg = _message.join(' ');

                const timer: Timers | null = await Arguments.Services.DB.timers.findFirst({
                    where: {
                        id: timer_id,
                        targetId: target.id
                    }
                });
                
                if (timer) {
                    return Promise.resolve("no");
                }

                await Arguments.Services.DB.timers.create({
                    data: {
                        id: timer_id,
                        targetId: target.id,
                        response: msg,
                        interval_ms: intervalsec,
                        value: true
                    }
                });

                if (Arguments.Services.Timer) Arguments.Services.Timer.newTick(
                    Arguments.Target.ID,
                    timer_id,
                    Arguments.Services.Client,
                    Arguments.Target.Username,
                    msg,
                    intervalsec
                );

                return Promise.resolve(await Arguments.Services.Locale.parsedText("timer.new", Arguments, [
                    timer_id
                ]));
            }

            case "--ring": {
                const timer: Timers | null = await Arguments.Services.DB.timers.findFirst({
                    where: {id: timer_id, targetId: target.id}
                });

                if (!timer) {
                    return Promise.resolve("no");
                }

                return Promise.resolve(timer.response);
            }

            case "--remove": {
                const timer: Timers | null = await Arguments.Services.DB.timers.findFirst({
                    where: {id: timer_id, targetId: target.id}
                });

                if (!timer) {
                    return Promise.resolve("no");
                }

                await Arguments.Services.DB.timers.delete({
                    where: {int_id: timer.int_id}
                });

                if (Arguments.Services.Timer) Arguments.Services.Timer.disposeTick(Arguments.Target.ID, timer_id);

                return Promise.resolve(await Arguments.Services.Locale.parsedText("timer.removed", Arguments, [
                    timer_id
                ]));
            }

            case "--disable": {
                const timer: Timers | null = await Arguments.Services.DB.timers.findFirst({
                    where: {id: timer_id, targetId: target.id}
                });

                if (!timer) {
                    return Promise.resolve("no");
                }

                await Arguments.Services.DB.timers.update({
                    where: {int_id: timer.int_id},
                    data: {
                        value: false
                    }
                });

                if (Arguments.Services.Timer) Arguments.Services.Timer.disposeTick(Arguments.Target.ID, timer_id);

                return Promise.resolve(await Arguments.Services.Locale.parsedText("timer.disabled", Arguments, [
                    timer_id
                ]));
            }

            case "--enable": {
                const timer: Timers | null = await Arguments.Services.DB.timers.findFirst({
                    where: {id: timer_id, targetId: target.id}
                });

                if (!timer) {
                    return Promise.resolve("no");
                }

                await Arguments.Services.DB.timers.update({
                    where: {int_id: timer.int_id},
                    data: {
                        value: true
                    }
                });

                if (Arguments.Services.Timer) Arguments.Services.Timer.newTick(
                    Arguments.Target.ID,
                    timer_id,
                    Arguments.Services.Client,
                    Arguments.Target.Username,
                    timer.response,
                    timer.interval_ms
                );

                return Promise.resolve(await Arguments.Services.Locale.parsedText("timer.enabled", Arguments, [
                    timer_id
                ]));
            }

            case "--info": {
                const timer: Timers | null = await Arguments.Services.DB.timers.findFirst({
                    where: {id: timer_id, targetId: target.id}
                });

                if (!timer) {
                    return Promise.resolve("no");
                }

                return Promise.resolve(await Arguments.Services.Locale.parsedText("timer.info", Arguments, [
                    timer_id,
                    (timer.value) ? "Enabled" : "Disabled",
                    timer.interval_ms.toString(),
                    "1",
                    timer.response
                ]));
            }

            case "--list": {
                const timer: Timers[] = await Arguments.Services.DB.timers.findMany({
                    where: {targetId: target.id}
                });

                if (timer.length === 0) {
                    return Promise.resolve("no");
                }

                return Promise.resolve(await Arguments.Services.Locale.parsedText("timer.list", Arguments, [
                    timer.map((t) => {
                        return t.id
                    }).join(", ")
                ]));
            }

            default: {
                return Promise.resolve(true);
            }
        }

    }
}