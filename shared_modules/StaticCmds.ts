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

        var _message: string[] = Arguments.Message.raw.split(' ');
        const option: string = _message[1];
        const static_id: string = _message[2];
        
        delete _message[0];
        delete _message[1];
        delete _message[2];

        switch (option) {
            case "--new": {
                Arguments.Services.StaticCmd.create(
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
                break;
            }
            case "--remove": {
                Arguments.Services.StaticCmd.remove(
                    Arguments.Target.ID,
                    static_id
                );
                break;
            } 
            case "--enable": {
                Arguments.Services.StaticCmd.enable(Arguments.Target.ID, static_id);
                break;
            }
            case "--disable": {
                Arguments.Services.StaticCmd.disable(Arguments.Target.ID, static_id);
                break;
            }
            case "--push": {
                var scmd = Arguments.Services.StaticCmd.get(Arguments.Target.ID, static_id);
                if (scmd === undefined) return Promise.resolve(false);
                if (scmd.Responses === undefined) return Promise.resolve(false);
                
                scmd.Responses.push(_message.join(' ').trim());
                break;
            }
            case "--list": {
                break;
            }
            case "--info": {
                break;
            }
            default: {
                break;
            }
        }

        return Promise.resolve(true);
    }
}