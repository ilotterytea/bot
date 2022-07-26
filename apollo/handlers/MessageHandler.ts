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
import IModule from "../interfaces/IModule";
import IStorage from "../interfaces/IStorage";
import EmoteUpdater from "../utils/emotes/EmoteUpdater";
import Localizator from "../utils/Locale";
import ModuleManager from "../utils/ModuleManager";

namespace Messages {
    export async function Handler(
        client: Client,
        api: TwitchApi.Client,
        storage: StoreManager,
        locale: Localizator,
        module: ModuleManager,
        stvemotes: EmoteUpdater.SevenTV
    ) {
        stvemotes.subscribe({
            client: client,
            localizator: locale,
            storage: storage,
            bot: {
                name: ""
            },
            target: {
                id: "",
                name: ""
            },
            user: {
                extRole: IModule.AccessLevels.PUBLIC,
                name: "",
                id: ""
            },
            message: {
                raw: "",
                command: ""
            },
            channel_emotes: {}
        });
        client.on("message", async (channel: string, user: ChatUserstate, message: string, self: boolean) => {
            if (self) return;
            // Create a new target file if the channel was created recently:
            if (!storage.targets.isExists(user["room-id"])) storage.targets.add(user["room-id"], {
                SuccessfullyCompletedTests: 0,
                ExecutedCommands: 0,
                ChatLines: 0,
                Emotes: {},
                Modules: {},
                Name: channel.slice(1, channel.length)
            });

            // +1 chat line to the target's file:
            storage.targets.edit(user["room-id"], "ChatLines", storage.targets.get(user["room-id"], "ChatLines") as number + 1);

            await stvemotes.levelUpEmote(message, channel.slice(1, channel.length));
            console.log(stvemotes.getEmote("test", channel.slice(1, channel.length)));

            if (message == "test") {
                storage.targets.edit(user["room-id"], "SuccessfullyCompletedTests", storage.targets.get(user["room-id"], "SuccessfullyCompletedTests") as number + 1);
                client.say(channel, locale.parsedText("test.test", user["room-id"]!, "test", storage.targets.get(user["room-id"], "SuccessfullyCompletedTests") as number));
            }

            console.log(storage.targets.get(user["room-id"]));

            if (message.startsWith(storage.getGlobalPrefix)) {
                var args: IArguments = {
                    client: client,
                    localizator: locale,
                    storage: storage,
                    bot: {
                        name: ""
                    },
                    target: {
                        id: user["room-id"]!,
                        name: channel
                    },
                    user: {
                        extRole: IModule.AccessLevels.PUBLIC,
                        name: user["username"]!,
                        id: user["user-id"]!
                    },
                    message: {
                        raw: message,
                        command: message.split(' ')[0].split(storage.getGlobalPrefix)[1]
                    },
                    channel_emotes: stvemotes.getAllChannelEmotes(channel.slice(1, channel.length))
                }

                if (storage.users.get(user["user-id"], "InternalType") === "supauser") args.user.extRole = IModule.AccessLevels.SUPAUSER;
                else {
                    if (args.user.id === args.target.id) args.user.extRole = IModule.AccessLevels.BROADCASTER;
                    if (user["badges"]?.moderator === "1") args.user.extRole = IModule.AccessLevels.MOD;
                    if (user["badges"]?.vip === "1") args.user.extRole = IModule.AccessLevels.VIP;
                }

                if (module.contains(args.message.command!)) {
                    var response = await module.call(args.message.command!, args);

                    if (typeof response == "boolean") {
                        return;
                    }
                    
                    args.storage.targets.edit(
                        user["room-id"]!, "ExecutedCommands",
                        args.storage.targets.get(user["room-id"]!, "ExecutedCommands") as number + 1
                    );

                    return client.say(channel, response as string);
                }
            }
        });

        // Save local files:
        setInterval(() => {
            storage.save(stvemotes.getEmotes);
        }, 10000);
    }

    export async function StaticCommandHandler(args: IArguments) {

    }
}

export default Messages;