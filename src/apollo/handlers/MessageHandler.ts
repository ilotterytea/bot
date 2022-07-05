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

import { ChatUserstate, Client } from "tmi.js";
import { HelixApiGroup } from "twitch/lib/API/Helix/HelixApiGroup";
import ApolloConfiguration from "../ApolloConfiguration";
import Localizator from "../utils/Localization";
import ResolveOptions from "../utils/MOptionsResolver";
import StoreManager from "../utils/StoreManager";

class MessageHandler {
    client: Client;
    api: HelixApiGroup;
    storage: StoreManager.Storage;

    constructor (
        client: Client,
        api: HelixApiGroup
    ) {
        this.client = client;
        this.api = api;
        this.storage = new StoreManager.Storage("");
    }

    async HandleMessages() {
        this.client.on(
            "message",
            async (
                target: string,
                user: ChatUserstate,
                msg: string,
                self: boolean
            ) => {
                const args: ApolloConfiguration.Args = {
                    Client: this.client,
                    TargetChat: target.slice(1, target.length),
                    User: user,
                    Message: {
                        Raw: msg,
                        Command: msg.split(' ')[0],
                        Options: await ResolveOptions(msg),
                        Msg: msg
                    },
                    Storage: this.storage.getStoreData(),
                    TTVAPI: this.api,
                    LocalAPI: Localizator.Localizator,
                    StaticCMD: 
                }
            }
        );
    }

    async SubEvents() {
        
    }
}

export default MessageHandler;