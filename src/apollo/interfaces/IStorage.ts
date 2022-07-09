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
            Client: number[] | undefined,
            Anonymous: number[] | undefined
        },

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
        RoomID: number | null,
        Prefix: string | null,
        LanguageID: string | null,
        Emotes: Emote[] | null,
        Commands: Command[] | null,
        SuccessfulTests: number | null,
        ChatLines: number | null,
        ExecutedCommands: number | null
    }

    /**
     * Command Settings Interface (IStorage). Version 2.
     * @since 2.2.0
     * @author NotDankEnough
     */
    export interface Command {
        /**
         * Command ID.
         */
        ID: number | null,

        /**
         * Command type.
         */
        Type: CommandTypes | null,

        /**
         * Path to the script. Required if you select *SCRIPTED* in the *Type* parameter.
         */
        ScriptPath?: PathLike | undefined,

        /**
         * Command response. The bot will send messages in the order specified in this array. Required if you select *STATIC* in the *Type* parameter.
         */
        Response?: string[] | undefined
    }

    /**
     * Command types.
     * @since 2.2.0
     * @author NotDankEnough
     */
    export enum CommandTypes {
        /**
         * Use it for static command.
         */
        STATIC,
        
        /**
         * Use it for command that have a .ts script.
         */
        SCRIPTED
    }

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
}


export default IStorage;