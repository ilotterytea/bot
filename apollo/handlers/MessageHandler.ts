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

import {
    ChatUserstate,
    Client
} from "tmi.js";
import TwitchApi from "../clients/ApiClient";
import StoreManager from "../files/StoreManager";
import IArguments from "../interfaces/IArguments";
import Localizator from "../utils/Locale";
import ModuleManager from "../utils/ModuleManager";

namespace Messages {
    export async function Handler(
        client: Client,
        api: TwitchApi.Client,
        storage: StoreManager,
        locale: Localizator,
        module: ModuleManager
    ) {
        client.on("message", async (channel: string, user: ChatUserstate, message: string, self: boolean) => {
            if (self) return;
            
            if (message.startsWith(storage.getPrefix(user["room-id"]!))) {
                var args: IArguments = {
                    client: client,
                    localizator: locale,
                    storage: storage,
                    target: {
                        id: user["room-id"]!,
                        name: channel
                    },
                    user: {
                        id: user["user-id"]!
                    },
                    message: {
                        raw: message,
                        command: message.split(' ')[0].split(storage.getPrefix(user["room-id"]!))[1]
                    }
                }

                if (module.contains(args.message.command!)) {
                    var response = await module.call(args.message.command!, args);

                    if (response == false) {
                        return;
                    }

                    return client.say(channel, response as string);
                }
            }
        });
    }

    export async function TSCommandHandler(args: IArguments) {

    }
}

export default Messages;