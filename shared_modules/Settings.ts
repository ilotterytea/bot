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

import { Target } from "@prisma/client";
import IArguments from "../apollo/interfaces/IArguments";
import IModule from "../apollo/interfaces/IModule";

export default class Settings implements IModule.IModule {
    cooldownMs: number;
    permissions: number;
    constructor (cooldownMs: number, permissions: number) {
        this.cooldownMs = cooldownMs;
        this.permissions = permissions;
    }

    async run(Arguments: IArguments) {
        const _message: string[] = Arguments.Message.raw.split(' ');

        const target: Target | null = await Arguments.Services.DB.target.findFirst({
            where: {alias_id: parseInt(Arguments.Target.ID)}
        });

        if (!target) return Promise.resolve(false);

        switch (true) {
            case (_message.includes("--lang")): {
                if (_message.indexOf("--lang") + 1 > _message.length - 1) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.set.language.available", Arguments));
                }

                const value = _message[_message.indexOf("--lang") + 1];

                if (Arguments.Services.Locale.getLanguages === undefined) {
                    return Promise.resolve(false);
                }

                if (!(value in Arguments.Services.Locale.getLanguages)) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.set.language.not_found", Arguments, [value]));
                }

                await Arguments.Services.DB.target.update({
                    where: {id: target.id},
                    data: {
                        language_id: value
                    }
                });

                return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.set.language.success", Arguments));
            }
            case (_message.includes("--prefix")): {
                if (_message.indexOf("--prefix") + 1 > _message.length - 1) {
                    return Promise.resolve(await Arguments.Services.Locale.parsedText("msg.wrong_option", Arguments, ["--prefix", "[STRING]"]));
                }

                var value = _message[_message.indexOf("--prefix") + 1];

                value = value.replace("[space]", ' ');

                await Arguments.Services.DB.target.update({
                    where: {id: target.id},
                    data: {
                        prefix: value
                    }
                });

                return Promise.resolve(await Arguments.Services.Locale.parsedText("cmd.set.prefix.success", Arguments, [value]));
            }
            default: {
                break;
            }
        }
        

        return Promise.resolve(true);
    }
}