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

import { readFileSync } from "fs";
import { ChatUserstate, Client } from "tmi.js";
import { HelixApiGroup } from "twitch/lib/API/Helix/HelixApiGroup";
import Localizator from "../utils/files/Localization";
import StaticCommands from "../handlers/StaticCMDHandler";
import IStorage from "./IStorage";
import EmoteUpdater from "../utils/EmoteUpdater";

/**
 * Argument interface for commands.
 * @since 2.2.0
 * @author NotDankEnough
 */
interface IArguments {
    /**
     * Tmi.js client.
     */
    Client: Client | undefined,

    /**
     * Chat where the message was sent from. Incredibly how desirable, set this parameter without the start # (if any).
     */
    TargetChat: string | undefined,

    /**
     * User interface from Tmi.js.
     */
    User: ChatUserstate | undefined,

    /**
     * Message.
     */
    Message: {
        /**
         * Raw message *(e.g. !set --lang ru_ru)*.
         */
        Raw: string | undefined,

        /**
         * Command without a prefix. *(e.g. set)*
         */
        Command: string | undefined,
        
        /**
         * Options. *(e.g. --lang ru_ru)*
         */
        Options: string[] | undefined,

        /**
         * Full message without options and command.
         */
        Msg: string | undefined
    },

    /**
     * Storage.
     */
    Storage: IStorage.Main,

    /**
     * Twitch API.
     */
    TTVAPI: HelixApiGroup,
    
    /**
     * Localization manager.
     */
    LocalAPI: Localizator.Localizator,

    /**
     * Emote API.
     */
    EmoteAPI: EmoteUpdater.SevenTV,

    /**
     * Internal user's data.
     */
    InternalUser: {

        /**
         * User's internal role. Available roles at 2.2: dank, broadcaster, mod, vip, user.
         */
        Role: string | undefined
    }
}

export default IArguments;