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

import { Argument } from "commander";
import {
    ChatUserstate,
    Client
} from "tmi.js";
import TwitchApi from "../clients/ApiClient";
import LocalStorage from "../files/LocalStorage";
import IArguments from "../interfaces/IArguments";
import IModule from "../interfaces/IModule";
import IServices from "../interfaces/IServices";
import IStorage from "../interfaces/IStorage";
import EmoteUpdater from "../utils/emotes/EmoteUpdater";
import Localizator from "../utils/Locale";
import ModuleManager from "../utils/ModuleManager";
import TimerHandler from "./TimerHandler";

namespace Messages {
    /**
     * Twitch TMI message handler.
     * @param Services services.
     */
    export async function Handler(Services: IServices) {
        Services.Client.on("message", async (channel: string, user: ChatUserstate, message: string, self: boolean) => {
            if (self) return;

            // Command prefix:
            const prefix: string = Services.Storage.Targets.containsKey(user["room-id"]!, "Prefix") ? Services.Storage.Targets.get(user["room-id"]!, "Prefix") as string : Services.Storage.Global.getPrefix;
            
            // Arguments:
            var args: IArguments = {
                Services: Services,
                Sender: {
                    Username: user.username!,
                    ID: user["user-id"]!,
                    extRole: IModule.AccessLevels.PUBLIC
                },
                Target: {
                    Username: channel.slice(1, channel.length),
                    ID: user["room-id"]!,
                    Emotes: (Services.Emote !== undefined) ? Services.Emote.getEmotes[user["room-id"]!] : undefined
                },
                Message: {
                    raw: message,
                    command: message.split(' ')[0].split(prefix)[1]
                }
            }

            // Assigning the roles:
            if (Services.Storage.Users.contains(args.Sender.ID)) {
                const role = Services.Storage.Users.get(args.Sender.ID, "InternalType");
                if (role !== undefined) args.Sender.intRole = role as IStorage.InternalRoles;
            }

            if (args.Target.ID === args.Sender.ID) args.Sender.extRole = IModule.AccessLevels.BROADCASTER;
            if (user["badges"]?.moderator === "1") args.Sender.extRole = IModule.AccessLevels.MOD;
            if (user["badges"]?.vip === "1") args.Sender.extRole = IModule.AccessLevels.VIP;

            // +1 chat line to the target's file:
            Services.Storage.Targets.set(
                args.Target.ID,
                "ChatLines",
                Services.Storage.Targets.get(args.Target.ID, "ChatLines") as number + 1
            );

            // +1 used times to the emote:
            if (Services.Emote !== undefined) Services.Emote.increaseEmoteCount(args.Message.raw, args.Target.ID);

            // Complete a test:
            if (message == "test") {
                Services.Storage.Targets.set(
                    args.Target.ID,
                    "SuccessfullyCompletedTests",
                    Services.Storage.Targets.get(args.Target.ID, "SuccessfullyCompletedTests") as number + 1
                );

                Services.Client.say(
                    `#${args.Target.Username}`,
                    Services.Locale.parsedText("msg.test", args, [
                        "test",
                        Services.Storage.Targets.get(args.Target.ID, "SuccessfullyCompletedTests") as number
                    ])
                );
            }

            // Start processing the commands:
            if (message.startsWith(prefix)) {

                // Execute command if it exists:
                if (Services.Module === undefined) throw new Error("Cannot process the commands. No module instances assigned to services.");
                
                if (Services.Module.contains(args.Message.command)) {
                    const response: boolean | string = await Services.Module.call(args.Message.command, args);

                    if (typeof response === "boolean") return;

                    Services.Client.say(`#${args.Target.Username}`, response);
                }
            }
            
            await StaticCommandHandler(args);
        });

        // Save local files:
        setInterval(async () => {
            if (Services.Emote !== undefined && Services.Timer !== undefined && Services.StaticCmd !== undefined) {
                Services.Storage.save(Services.Emote.getEmotes, Services.Timer.getTimers, Services.StaticCmd.getCmds);
                return;
            }
            Services.Storage.save();
        }, 3600000);

        process.once("SIGHUP", async () => {
            await Services.Client.disconnect();
            
            if (Services.Emote !== undefined && Services.Timer !== undefined && Services.StaticCmd !== undefined) {
                Services.Storage.save(Services.Emote.getEmotes, Services.Timer.getTimers, Services.StaticCmd.getCmds);
                return;
            }
            
            Services.Storage.save();
        });
        setInterval(async () => {
            if (Services.Emote === undefined) return;

            for (const name of Object.keys(Services.Storage.Global.getSymlinks)) {
                await Services.Emote.syncBTTVEmotes(name, false);
                await Services.Emote.syncFFZEmotes(name, false);
                await Services.Emote.syncTTVEmotes(name, false);
            }
        }, 30000);
        if (Services.Timer !== undefined) Services.Timer.tick(Services.Client);
    }

    async function StaticCommandHandler(args: IArguments) {
        if (args.Services.StaticCmd === undefined) return;

        var cmd = args.Services.StaticCmd.get(
            args.Target.ID,
            args.Message.raw.split(' ')[0]
        );

        if (cmd === undefined) return;
        if (cmd.Responses === undefined) return;

        for (const msg of cmd.Responses) {
            args.Services.Client.say(`#${args.Target.Username}`, msg);
        }
    }
}

export default Messages;