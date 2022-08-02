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

export default class EmoteTop implements IModule.IModule {
    cooldownMs: number;
    permissions: IModule.AccessLevels;
    minPermissions?: IModule.AccessLevels;

    constructor (cooldownMs: number, perms: IModule.AccessLevels, minperms?: IModule.AccessLevels | undefined) {
        this.cooldownMs = cooldownMs;
        this.permissions = perms;
        this.minPermissions = minperms;
    }

    async run(Arguments: IArguments) {
        const _message = Arguments.message.raw!.split(' ');
        type TopModes = "descending" | "ascending";
        
        // !etop -m descending/ascending -s 1-15
        var mode: TopModes = "descending";
        var show_emotes: number = 15;        

        if (_message.includes('-m')) {
            if ((_message.indexOf('-m') + 1 ) <= _message.length - 1) {
                const _mode: string = _message[_message.indexOf('-m') + 1].toLowerCase();
                switch (_mode) {
                    case "descending":
                        mode = "descending";
                        break;
                    case "ascending":
                        mode = "ascending";
                        break;
                    default:
                        mode = "descending";
                        break;
                }
            }
        }

        if (_message.includes('-s')) {
            if ((_message.indexOf('-s') + 1 ) > _message.length - 1) return Promise.resolve(
                Arguments.localizator!.parsedText("msg.wrong_option", Arguments, [
                    "-s",
                    "[INTEGER]"
                ])
            );
            var _show_emotes: number = parseInt(_message[_message.indexOf('-s') + 1]);
            if (isNaN(_show_emotes)) return Promise.resolve(
                Arguments.localizator!.parsedText("msg.wrong_option", Arguments, [
                    "-s",
                    "[INTEGER]"
                ])
            );
            show_emotes = _show_emotes;
        }

        if (show_emotes > Object.keys(Arguments.channel_emotes!).length) {
            show_emotes = Object.keys(Arguments.channel_emotes!).length;
        }

        var items = Object.keys(Arguments.channel_emotes!).map((key) => {
            return [key, Arguments.channel_emotes![key].UsedTimes];
        });

        var top: (string|number)[][] = [];

        switch (mode) {
            case "descending":
                items.sort((f, s) => {
                    return parseInt(s[1] as string) - parseInt(f[1] as string);
                });
                top = items.slice(0, show_emotes);
                break;
            case "ascending":
                items.sort((f, s) => {
                    return parseInt(f[1] as string) - parseInt(s[1] as string);
                });
                top = items.slice(0, show_emotes);
                break;
            default:
                items.sort((f, s) => {
                    return parseInt(s[1] as string) - parseInt(f[1] as string);
                });
                top = items.slice(0, show_emotes);
                break;
        }

        var text: string = ``;

        for (var i = 0; i < show_emotes; i++) {
            text = text + `${top[i][0]} (${top[i][1]})${(i + 1 >= show_emotes) ? "" : ", "}`;
        }

        return Promise.resolve(Arguments.localizator!.parsedText("cmd.etop.response", Arguments, [
            show_emotes.toString(),
            "7TV",
            Arguments.localizator!.parsedText(
                (mode === "descending") ? "mode.descending" : "mode.ascending",
                Arguments
            ),
            text
        ]));
    }
}