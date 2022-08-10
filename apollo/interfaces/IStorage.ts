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

import { EmoteProviders } from "../types/SmolTypes"
import IModule from "./IModule"

/** @deprecated */
namespace IStorage {
    /** @deprecated */
    export interface V1Main {
        version: string,
        join: {
            asanonymous: string[],
            asclient: string[]
        },
        prefix: string,
        emotes: {
            [target_name: string]: {
                [emote_name: string]: number
            }
        },
        roles: {
            [role_name: string]: number[]
        },
        stats: {
            chat_lines: {[target_name: string]: number},
            executed_commands: {[target_name: string]: number},
            tests: {[target_name: string]: number}
        },
        preferred: {
            [target_name: string]: {
                lang: string
            }
        }
    }

    export interface Main {
        Version: string,
        Join: {
            AsClient: number[],
            AsAnonymous: number[]
        },
        Global: {
            Prefix: string,
            Modules: Module[],
            Users: User[]
        }
    }

    /** @deprecated */
    export interface Target {
        Name?: string,
        LanguageId?: string | undefined,
        Modules?: Module[],
        Timers?: {[timer_id: string]: Timer} | undefined,
        Prefix?: string,
        ChatLines?: number | undefined,
        SuccessfullyCompletedTests?: number | undefined,
        ExecutedCommands?: number | undefined,
        AuthToken?: string | undefined,
        Emotes?: {[provider_id: string]: Emote[]}
    }

    /** @deprecated */
    export interface Timer{
        Value: boolean,
        Response: string[],
        IntervalMs: number
    }

    /**
     * 7TV/BetterTTV/FrankerFaceZ emote interface.
     * @deprecated
     */
    export interface Emote {
        /** Name of the emote. */
        Name: string,

        /** ID of the emote. */
        ID: string,

        /** History of emote name. */
        NameHistory?: string[],
        
        /** How many times was the emote used? */
        UsedTimes: number,
        
        /** Is emote deleted from the chat room? */
        isDeleted?: boolean | undefined,

        /** Is emote global? */
        isGlobal?: boolean | undefined
    }

    /** @deprecated */
    export interface Module {
        Value: boolean,
        ID: string,
        Type: ModuleTypes,
        ScriptPath?: string | undefined,
        Responses?: string[] | undefined
    }

    /** @deprecated */
    export interface User {
        ID: string,
        InternalType?: InternalRoles | undefined,
        ExternalType?: IModule.AccessLevels | undefined
    }

    export enum InternalRoles {
        SUSPENDED = -1,
        SUPAUSER = 99
    }

    /** @deprecated */
    type ModuleTypes = "scripted" | "static";
}

export default IStorage;