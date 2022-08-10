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
import IModule from "./IModule";
import IServices from "./IServices";
import IStorage from "./IStorage";

/** Arguments. */
interface IArguments {
    /** Services. */
    Services: IServices

    /** Bot info. */
    Bot?: {
        /** Bot's username. */
        Username: string
    },

    /** Sender info. */
    Sender: {
        /** Sender's username. */
        Username: string,

        /** Sender's Twitch ID. */
        ID: string,

        /** Sender's internal role. */
        intRole?: IStorage.InternalRoles | undefined,

        /** Sender's external role. */
        extRole?: IModule.AccessLevels | undefined
    },

    /** Channel info. */
    Target: {
        /** Channel's name. */
        Username: string,

        /** Channel's Twitch ID. */
        ID: string
    },

    /** Message. */
    Message: {
        /** Raw message. */
        raw: string,

        /** Command. */
        command: string,

        /** Options. */
        options?: {option: string, value: string}[] | undefined
    },

    /** Global settings. */
    Global?: Target
}

export default IArguments;