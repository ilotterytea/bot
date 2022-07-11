// Copyright (C) 2022 ilotterytea
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

import { PathLike } from "fs"

/**
 * Storage interface.
 * @since 2.2.0
 * @author NotDankEnough
 */
namespace IStorage {
    /**
     * Main Storage Interface. Version 2.
     * @since 2.2.0
     * @author NotDankEnough
     */
    export interface Main {
        
        /**
         * Storage version.
         */
        Version: string | undefined,

        /**
         * Array with **user IDs**. This is necessary so that if the owner of the channel changed his nickname, the bot immediately joined the channel.
         */
        Join: {
            Client: number[],
            Anonymous: number[]
        },

        Global: {
            Target: Target,
            Users: User[]
        }

        /**
         * Channels.
         */
        Targets: Target[]
    }

    /**
     * Channel Interface (IStorage). Version 2.
     * @since 2.2.0
     * @author NotDankEnough
     */
    export interface Target {
        
        /**
         * Chat room ID. Get it from *ChatUserstate["room-id"]*.
         */
        RoomID?: number | null,

        /**
         * Chat room prefix.
         */
        Prefix?: string | null,

        /**
         * Chat room preferred language.
         */
        LanguageID?: string | null,

        /**
         * Chat room emotes.
         */
        Emotes?: Emote[] | null,

        /**
         * Chat room static, scripted commands, modules.
         */
        Modules?: Module[] | null,

        /**
         * Successful tests in chat room.
         */
        SuccessfulTests?: number | null,

        /**
         * Number of collected chat lines in chat room.
         */
        ChatLines?: number | null,

        /**
         * Number of executed chat commands in chat room.
         */
        ExecutedCommands?: number | null
    }

    export interface Module {

    }

    /**
     * Module Settings Interface (IStorage). Version 2.
     * @since 2.2.0
     * @author NotDankEnough
     */
    export interface Module {

        /**
         * Enable/disable the module.
         */
        Value: boolean,

        /**
         * Module ID.
         */
        ID: string,

        /**
         * Module type.
         */
        Type: ModuleTypes,

        /**
         * Path to the script. Required if you select *SCRIPTEDCMD* in the *Type* parameter.
         */
        ScriptPath?: PathLike | undefined,

        /**
         * Module response. The bot will send messages in the order specified in this array. Required if you select *STATICCMD* in the *Type* parameter.
         */
        Response?: string[] | undefined
    }

    /**
     * Module types.
     * @since 2.2.0
     * @author NotDankEnough
     */
    export type ModuleTypes = "scriptedcmd" |
        "staticcmd" | "module"

    /**
     * Emote Interface (IStorage). Version 2.
     * @since 2.2.0
     * @author NotDankEnough
     */
    export interface Emote {
        /**
         * Emote name.
         */
        Name: string | undefined,

        /**
         * Emote ID from its provider.
         */
        ExternalID: string | undefined,

        /**
         * The number of times the emote has been used by the user.
         */
        UsedTimes: number | undefined
    }

    export interface User {
        ID: number,
        Role?: UserRoles,
        moduleAccess?: Module["ID"][]
    }

    export type UserRoles = "superuser" |
        "broadcaster" |
        "mod" | 
        "special" |
        "vip" |
        "user" |
        "suspended";
}


export default IStorage;