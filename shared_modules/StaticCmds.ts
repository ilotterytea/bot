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

export default class StaticCmds implements IModule.IModule {
    cooldownMs: number;
    permissions: number;

    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {
        if (Arguments.Services.StaticCmd === undefined) throw new Error("");

        // !scmd --edit bruh 3 kek
        const max_lines: number = 10;
        var _message: string[] = Arguments.Message.raw.split(' ');
        const option: string = _message[1];
        var static_id: string = _message[2];

        delete _message[0];
        delete _message[1];
        delete _message[2];

        switch (option) {
            case "--new": {
                const response = Arguments.Services.StaticCmd.create(
                    Arguments.Target.ID,
                    {
                        Value: true,
                        ID: static_id,
                        Responses: [
                            _message.join(' ').trim()
                        ],
                        Type: "static"
                    }
                );

                if (!response) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.already_exists", Arguments, [
                            static_id
                    ]));
                }
                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.new", Arguments, [
                        static_id,
                        Arguments.Target.Username
                ]));
            }
            case "--remove": {
                const response = Arguments.Services.StaticCmd.remove(
                    Arguments.Target.ID,
                    static_id
                );
                if (!response) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                            static_id
                    ]));
                }
                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.remove", Arguments, [
                        static_id,
                        Arguments.Target.Username
                ]));
            } 
            case "--enable": {
                const response = Arguments.Services.StaticCmd.enable(Arguments.Target.ID, static_id);
                if (!response) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                            static_id
                    ]));
                }
                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.enable", Arguments, [
                        static_id
                ]));
            }
            case "--disable": {
                const response = Arguments.Services.StaticCmd.disable(Arguments.Target.ID, static_id);
                if (!response) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                            static_id
                    ]));
                }
                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.disable", Arguments, [
                        static_id
                ]));
            }
            case "--push": {
                var response = Arguments.Services.StaticCmd.get(Arguments.Target.ID, static_id);
                if (response === undefined) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));
                
                if (response.Type != "static") return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));

                if (response.Responses === undefined) response.Responses = [];

                if (response.Responses.length + 1 > max_lines) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.push.limit_reached", Arguments, [
                    max_lines
                ]));

                response.Responses.push(
                    _message.join(' ').trim()
                );
                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.push", Arguments, [
                    _message.join(' ').trim(),
                    static_id
                ]));
            }
            case "--editline": {
                var response = Arguments.Services.StaticCmd.get(Arguments.Target.ID, static_id);
                const line_id = _message[3];

                if (isNaN(parseInt(line_id))) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_a_line_id", Arguments, [
                        line_id
                    ]));
                }

                delete _message[3];

                if (response === undefined) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));
                
                if (response.Type != "static") return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));

                if (response.Responses === undefined) response.Responses = [];

                if (parseInt(line_id) > response.Responses.length) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.line_not_exists", Arguments, [
                    line_id,
                    static_id
                ])); 

                response.Responses[parseInt(line_id) - 1] = _message.join(' ').trim();

                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.editline", Arguments, [
                    line_id,
                    static_id
                ]));
            }
            case "--rmline": {
                var response = Arguments.Services.StaticCmd.get(Arguments.Target.ID, static_id);
                const line_id = _message[3];

                if (isNaN(parseInt(line_id))) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_a_line_id", Arguments, [
                        line_id
                    ]));
                }

                delete _message[3];

                if (response === undefined) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));
                
                if (response.Type != "static") return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                    static_id
                ]));

                if (response.Responses === undefined) response.Responses = [];

                if (parseInt(line_id) > response.Responses.length) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.line_not_exists", Arguments, [
                    line_id,
                    static_id
                ])); 

                delete response.Responses[parseInt(line_id)];
                response.Responses = response.Responses.filter(r => r !== null);

                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.rmline", Arguments, [
                    line_id,
                    _message.join(' ').trim(),
                    static_id
                ]));
            }
            case "--copy": {
                var user_id: string | undefined = (static_id in Arguments.Services.Storage.Global.getSymlinks) ? Arguments.Services.Storage.Global.getSymlinks[static_id] : undefined;

                if (user_id === undefined) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.copy.target_not_exists", Arguments, [
                        static_id
                    ]));
                }

                if (_message[3] === undefined) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.copy.command_not_specified", Arguments));
                }

                var response = Arguments.Services.StaticCmd.get(user_id, _message[3]);

                if (response === undefined) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.copy.command_not_exists", Arguments, [
                    _message[3],
                    static_id
                ]));

                Arguments.Services.StaticCmd.create(Arguments.Target.ID, response);

                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.copy", Arguments, [
                    _message[3],
                    static_id
                ]));
            }
            // !scmd --list ilotterytea
            case "--list": {
                var user_id: string | undefined = undefined;

                if (static_id in Arguments.Services.Storage.Global.getSymlinks) {
                    if (Arguments.Services.Storage.Global.getSymlinks[static_id] in Arguments.Services.StaticCmd.getCmds) {
                        user_id = Arguments.Services.Storage.Global.getSymlinks[static_id];
                    }
                }

                if (user_id === undefined) {
                    user_id = Arguments.Target.ID;
                    static_id = Arguments.Target.Username;
                }

                const commands = Arguments.Services.StaticCmd.getCmds[user_id];

                if (commands.length == 0) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.list.no_cmds", Arguments, [
                    static_id
                ]));

                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.list", Arguments, [
                    commands.length,
                    commands.map((c) => {
                        return c.ID
                    }).join(', '),
                    static_id
                ]))
            }
            case "--info": {
                if (static_id === undefined) return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.specify_command", Arguments));
                const response = Arguments.Services.StaticCmd.get(Arguments.Target.ID, static_id);

                if (response === undefined) {
                    return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.not_exists", Arguments, [
                        static_id
                    ]));
                }

                if (response.Responses === undefined) return Promise.resolve(false);

                return Promise.resolve(Arguments.Services.Locale.parsedText("staticcmd.info", Arguments, [
                    response.ID,
                    response.Value,
                    response.Responses.length,
                    response.Responses.map((r) => {
                        return `"${r}"`
                    }).join(', ')
                ]));
            }
            default: {
                break;
            }
        }

        return Promise.resolve(true);
    }
}