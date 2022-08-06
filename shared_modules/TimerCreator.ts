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
                    return Promise.resolve(Arguments.Services.Locale.parsedText("timer.incorrect_interval", Arguments, [
                        timer_id,
                        intervalsec
                    ]));
                }
                
                const msg = _message.join(' ');

                const resp = Arguments.Services.Timer.createTimer(
                    Arguments.Target.ID,
                    timer_id,
                    {
                        Value: true,
                        Response: [
                            msg.trim()
                        ],
                        IntervalMs: intervalsec
                    },
                    Arguments.Services.Client
                );
                
                if (!resp) {
                    return Promise.resolve("no");
                }
                return Promise.resolve(Arguments.Services.Locale.parsedText("timer.new", Arguments, [
                    timer_id
                ]));
            }

            case "--ring": {
                const resp = Arguments.Services.Timer.getTimer(Arguments.Target.ID, timer_id);

                if (!resp) {
                    return Promise.resolve("no");
                }
                
                for (const msg of resp.Response) {
                    Arguments.Services.Client.say(Arguments.Target.Username, msg);
                }

                return Promise.resolve(true);
            }

            case "--remove": {
                const resp = Arguments.Services.Timer.removeTimer(Arguments.Target.ID, timer_id);

                if (!resp) {
                    return Promise.resolve("no");
                }
                return Promise.resolve(Arguments.Services.Locale.parsedText("timer.removed", Arguments, [
                    timer_id
                ]));
            }

            case "--disable": {
                const resp = Arguments.Services.Timer.disableTimer(Arguments.Target.ID, timer_id);

                if (!resp) {
                    return Promise.resolve("no");
                }
                return Promise.resolve(Arguments.Services.Locale.parsedText("timer.disabled", Arguments, [
                    timer_id
                ]));
            }

            case "--enable": {
                const resp = Arguments.Services.Timer.enableTimer(Arguments.Target.ID, timer_id, Arguments);

                if (!resp) {
                    return Promise.resolve("no");
                }
                return Promise.resolve(Arguments.Services.Locale.parsedText("timer.enabled", Arguments, [
                    timer_id
                ]));
            }

            case "--info": {
                const resp = Arguments.Services.Timer.getTimer(Arguments.Target.ID, timer_id);
            
                if (!resp) {
                    return Promise.resolve("no");
                }

                var _responses: string[] = [];

                resp.Response.forEach((response) => {
                    _responses.push(`'${response}'`);
                });

                return Promise.resolve(Arguments.Services.Locale.parsedText("timer.info", Arguments, [
                    timer_id,
                    (resp.Value) ? "Enabled" : "Disabled",
                    resp.IntervalMs.toString(),
                    resp.Response.length.toString(),
                    _responses.join(', ')
                ]));
            }

            case "--list": {
                var timers: string[] = Object.keys(Arguments.Services.Timer.getTimers[Arguments.Target.ID]);

                return Promise.resolve(Arguments.Services.Locale.parsedText("timer.list", Arguments, [
                    timers.join(', '),
                    (Arguments.Services.Storage.Targets.containsKey(Arguments.Target.ID, "Prefix")) ? 
                    Arguments.Services.Storage.Targets.get(Arguments.Target.ID, "Prefix") as string :
                    Arguments.Services.Storage.Global.getPrefix
                ]));
            }

            default: {
                return Promise.resolve(true);
            }
        }

    }
}