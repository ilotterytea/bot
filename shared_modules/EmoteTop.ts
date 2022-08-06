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
import { EmoteProviders } from "../apollo/types/SmolTypes";

export default class EmoteTop implements IModule.IModule {
    cooldownMs: number;
    permissions: number;
    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {
        if (Arguments.Target.Emotes === undefined) return Promise.resolve(false);
        const _message = Arguments.Message.raw.split(' ');
        type TopModes = "descending" | "ascending";
        
        // !etop -m descending/ascending -s 1-15 -p bttv
        var mode: TopModes = "descending";
        var show_emotes: number = 10;

        var provider: EmoteProviders | string = "stv";
        var provider_name: string = "7TV";

        if (_message.includes('-p')) {
            if ((_message.indexOf('-p') + 1 ) <= _message.length - 1) {
                const _provider: string = _message[_message.indexOf('-p') + 1].toLowerCase();
                
                switch (_provider) {
                    case "7tv" || "seventv": {
                        provider = "stv";
                        provider_name = "7TV";
                        break;
                    }
                    case "bttv" || "betterttv": {
                        provider = "bttv";
                        provider_name = "BetterTTV";
                        break;
                    }
                    case "ffz" || "frankerfacez": {
                        provider = "ffz";
                        provider_name = "FrankerFaceZ";
                        break;
                    }
                    case "ttv" || "twitch": {
                        provider = "ttv";
                        provider_name = "Twitch";
                        break;
                    }
                    default: {
                        provider = "stv";
                        provider_name = "7TV";
                        break;
                    }
                }
                
            }
        }

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
                Arguments.Services.Locale.parsedText("msg.wrong_option", Arguments, [
                    "-s",
                    "[INTEGER]"
                ])
            );
            var _show_emotes: number = parseInt(_message[_message.indexOf('-s') + 1]);
            if (isNaN(_show_emotes)) return Promise.resolve(
                Arguments.Services.Locale.parsedText("msg.wrong_option", Arguments, [
                    "-s",
                    "[INTEGER]"
                ])
            );
            if (_show_emotes > 15) {
                return Promise.resolve(Arguments.Services.Locale.parsedText("cmd.etop.limit_reached", Arguments, [
                    15,
                    Arguments.Target.ID
                ]));
            }
            show_emotes = _show_emotes;
        }

        if (show_emotes > Arguments.Target.Emotes[provider].length) {
            show_emotes = Arguments.Target.Emotes[provider].length;
        }

        var items = Arguments.Target.Emotes[provider].map((key) => {
            return [key.Name, key.UsedTimes];
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

        return Promise.resolve(Arguments.Services.Locale.parsedText("cmd.etop.response", Arguments, [
            show_emotes.toString(),
            provider_name,
            Arguments.Services.Locale.parsedText(
                (mode === "descending") ? "mode.descending" : "mode.ascending",
                Arguments
            ),
            text
        ]));
    }
}