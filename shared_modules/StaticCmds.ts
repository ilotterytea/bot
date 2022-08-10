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

import { CustomResponses, Target } from "@prisma/client";
import IArguments from "../apollo/interfaces/IArguments";
import IModule from "../apollo/interfaces/IModule";

export default class StaticCmds implements IModule.IModule {
    cooldownMs: number;
    permissions: number;

    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {

        // !scmd --edit bruh 3 kek
        const max_lines: number = 10;
        var _message: string[] = Arguments.Message.raw.split(' ');
        const option: string = _message[1];
        var static_id: string = _message[2];

        const target: Target | null = await Arguments.Services.DB.target.findFirst({
            where: {alias_id: parseInt(Arguments.Target.ID)}
        });

        if (!target) return Promise.resolve(false);

        delete _message[0];
        delete _message[1];
        delete _message[2];

        switch (option) {
            case "--new": {
                const cmd: CustomResponses | null = await Arguments.Services.DB.customResponses.findFirst({
                    where: {
                        id: static_id,
                        targetId: target.id
                    }
                });

                if (cmd) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.already_exists", Arguments, [
                        static_id
                    ]));
                }

                await Arguments.Services.DB.customResponses.create({
                    data: {
                        id: static_id,
                        response: _message.join(' ').trim(),
                        value: true,
                        targetId: target.id
                    }
                });

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.new", Arguments, [
                        static_id,
                        Arguments.Target.Username
                ]));
            }
            case "--remove": {
                const cmd: CustomResponses | null = await Arguments.Services.DB.customResponses.findFirst({
                    where: {
                        id: static_id,
                        targetId: target.id
                    }
                });

                if (!cmd) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                            static_id
                    ]));
                }

                await Arguments.Services.DB.customResponses.delete({
                    where: {int_id: cmd.int_id}
                });

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.remove", Arguments, [
                        static_id,
                        Arguments.Target.Username
                ]));
            } 
            case "--enable": {
                const cmd: CustomResponses | null = await Arguments.Services.DB.customResponses.findFirst({
                    where: {
                        id: static_id,
                        targetId: target.id
                    }
                });

                if (!cmd) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                            static_id
                    ]));
                }

                await Arguments.Services.DB.customResponses.update({
                    where: {
                        int_id: cmd.int_id
                    }, data: {
                        value: true
                    }
                });

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.enable", Arguments, [
                        static_id
                ]));
            }
            case "--disable": {
                const cmd: CustomResponses | null = await Arguments.Services.DB.customResponses.findFirst({
                    where: {
                        id: static_id,
                        targetId: target.id
                    }
                });

                if (!cmd) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                            static_id
                    ]));
                }

                await Arguments.Services.DB.customResponses.update({
                    where: {int_id: cmd.int_id},
                    data: {
                        value: false
                    }
                });

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.disable", Arguments, [
                        static_id
                ]));
            }/*
            case "--push": {
                var response = Arguments.Services.StaticCmd.get(Arguments.Target.ID, static_id);
                if (response === undefined) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));
                
                if (response.Type != "static") return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));

                if (response.Responses === undefined) response.Responses = [];

                if (response.Responses.length + 1 > max_lines) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.push.limit_reached", Arguments, [
                    max_lines
                ]));

                response.Responses.push(
                    _message.join(' ').trim()
                );
                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.push", Arguments, [
                    _message.join(' ').trim(),
                    static_id
                ]));
            }
            case "--editline": {
                var response = Arguments.Services.StaticCmd.get(Arguments.Target.ID, static_id);
                const line_id = _message[3];

                if (isNaN(parseInt(line_id))) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_a_line_id", Arguments, [
                        line_id
                    ]));
                }

                delete _message[3];

                if (response === undefined) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));
                
                if (response.Type != "static") return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));

                if (response.Responses === undefined) response.Responses = [];

                if (parseInt(line_id) > response.Responses.length) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.line_not_exists", Arguments, [
                    line_id,
                    static_id
                ])); 

                response.Responses[parseInt(line_id) - 1] = _message.join(' ').trim();

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.editline", Arguments, [
                    line_id,
                    static_id
                ]));
            }
            case "--rmline": {
                var response = Arguments.Services.StaticCmd.get(Arguments.Target.ID, static_id);
                const line_id = _message[3];

                if (isNaN(parseInt(line_id))) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_a_line_id", Arguments, [
                        line_id
                    ]));
                }

                delete _message[3];

                if (response === undefined) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));
                
                if (response.Type != "static") return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));

                if (response.Responses === undefined) response.Responses = [];

                if (parseInt(line_id) > response.Responses.length) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.line_not_exists", Arguments, [
                    line_id,
                    static_id
                ])); 

                delete response.Responses[parseInt(line_id)];
                response.Responses = response.Responses.filter(r => r !== null);

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.rmline", Arguments, [
                    line_id,
                    _message.join(' ').trim(),
                    static_id
                ]));
            }*/
            case "--edit": {
                const cmd: CustomResponses | null = await Arguments.Services.DB.customResponses.findFirst({
                    where: {
                        id: static_id,
                        targetId: target.id
                    }
                });

                if (!cmd) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                        static_id
                    ]));
                }

                await Arguments.Services.DB.customResponses.update({
                    where: {int_id: cmd.int_id},
                    data: {
                        response: _message.join(' ').trim()
                    }
                });

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.editline", Arguments, [
                    "1",
                    static_id
                ]));
            }
            case "--copy": {
                var user_id: Target | null = await Arguments.Services.DB.target.findFirst({
                    where: {
                        alias_id: (Arguments.Services.Symlinks.containsSymlink(static_id)) ? parseInt(Arguments.Services.Symlinks.getSymlink(static_id)!) : -1
                    }
                });

                if (user_id === null) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.copy.target_not_exists", Arguments, [
                        static_id
                    ]));
                }

                if (_message[3] === undefined) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.copy.command_not_specified", Arguments));
                }

                const cmd: CustomResponses | null = await Arguments.Services.DB.customResponses.findFirst({
                    where: {
                        id: _message[3],
                        targetId: user_id.id
                    }
                });

                if (!cmd) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.copy.command_not_exists", Arguments, [
                    _message[3],
                    static_id
                ]));

                await Arguments.Services.DB.customResponses.create({
                    data: {
                        targetId: target.id,
                        id: cmd.id,
                        response: cmd.response,
                        value: true
                    }
                });

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.copy", Arguments, [
                    _message[3],
                    static_id
                ]));
            }
            // !scmd --list ilotterytea
            case "--list": {
                var user_id: Target | null = await Arguments.Services.DB.target.findFirst({
                    where: {
                        alias_id: parseInt(Arguments.Services.Symlinks.getSymlink(static_id)!)
                    }
                });

                if (!user_id) {
                    user_id = target;
                }

                const commands: CustomResponses[] = await Arguments.Services.DB.customResponses.findMany({
                    where: {targetId: user_id.id}
                });

                if (commands.length == 0) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.list.no_cmds", Arguments, [
                    static_id
                ]));

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.list", Arguments, [
                    commands.length,
                    commands.map((c) => {
                        return c.id
                    }).join(', '),
                    static_id
                ]))
            }
            case "--info": {
                if (static_id === undefined) return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.specify_command", Arguments));
                const cmd: CustomResponses | null = await Arguments.Services.DB.customResponses.findFirst({
                    where: {
                        id: static_id,
                        targetId: target.id
                    }
                });

                if (!cmd) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                        static_id
                    ]));
                }

                return Promise.resolve(await Arguments.Services.Locale.parsedText("staticcmd.info", Arguments, [
                    cmd.id,
                    cmd.value,
                    "1",
                    `"${cmd.response}"`
                ]));
            }
            default: {
                break;
            }
        }

        return Promise.resolve(true);
    }
}