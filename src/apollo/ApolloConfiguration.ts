// Copyright (C) 2022 ilotterytea
// 
// This file is part of iLotteryteaLive.
// 
// iLotteryteaLive is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// iLotteryteaLive is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with iLotteryteaLive.  If not, see <http://www.gnu.org/licenses/>.

import dotenv from "dotenv";
import { readFileSync } from "fs";
import { ChatUserstate, Client } from "tmi.js";
import { HelixApiGroup } from "twitch/lib/API/Helix/HelixApiGroup";
import pck from "../../package.json";
import Localizator from "./utils/Localization";

import StoreManager from "./utils/StoreManager";

dotenv.config({
    path: "../../.env"
});

namespace ApolloConfiguration {
    export interface Credentials {
        /**
         * Bot's username.
         */
        Username: string | undefined,
    
        /**
         * Bot's OAuth token from https://twitchapps.com/tmi.
         */
        Password: string | undefined,
    
        /**
         * Client ID from your Twitch Developers Application.
         */
        ClientID: string | undefined,
    
        /**
         * Client Secret from your Twitch Developers Application.
         */
        ClientSecret: string | undefined,
    
        /**
         * Access token when you're authenticating in your Twitch Developers Application.
         */
        AccessToken: string | undefined,
    
        /**
         * API key from server. Required for use the "!iu save <LINK>".
         * More info can be found here: https://hmmtodayiwill.ru/api/key
         */
        //IOKeyAPI?: string | undefined
    }

    
    
    export interface Args {
        Client: Client | undefined,
        TargetChat: string | undefined,
        User: ChatUserstate | undefined,
        Message: {
            Raw: string | undefined,
            Command: string | undefined,
            Options: string[] | undefined,
            Msg: string | undefined
        },
        Storage: StoreManager.IStorage,
        TTVAPI: HelixApiGroup,
        LocalAPI: Localizator.Localizator
        StaticCMD: undefined,
        EmotesAPI: undefined,
        InternalUser: {
            Role: string | undefined
        }
    }
}


export default ApolloConfiguration;