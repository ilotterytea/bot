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

import { CustomResponses, Target, User } from "@prisma/client";
import { Argument } from "commander";
import {
    ChatUserstate,
    Client
} from "tmi.js";
import { Logger } from "tslog";
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

const log: Logger = new Logger({name: "Messages"});

namespace Messages {
    /**
     * Twitch TMI message handler.
     * @param Services services.
     */
    export async function Handler(Services: IServices) {
        Services.Client.on("message", async (channel: string, user: ChatUserstate, message: string, self: boolean) => {
            if (self) return;

            // Command prefix:
            const targetDb: Target | null = await Services.DB.target.findFirst({
                where: {
                    alias_id: parseInt(user["room-id"]!)
                }
            });

            const userDb: User | null = await Services.DB.user.findFirst({
                where: {alias_id: parseInt(user["user-id"]!)}
            });

            const globalTarget: Target | null = await Services.DB.target.findFirst({
                where: {id: -72, alias_id: -71}
            });

            // Don't continue processing the code if sender is suspended from the bot:
            if (userDb !== null && userDb.int_role !== null && userDb.int_role == -1) return;

            if (!globalTarget) {
                log.warn("Global target (ID -72) not found! Creating...");
                await Services.DB.target.create({
                    data: {
                        id: -72,
                        alias_id: -71,
                        chat_lines: 0,
                        exec_cmds: 0,
                        tests: 0,
                        language_id: "en_us",
                        prefix: "!"
                    }
                });
                return;
            }

            if (!targetDb) {
                log.warn("Target with ID", user["room-id"]!, "not found in database! Creating a new one...");
                await Services.DB.target.create({
                    data: {
                        alias_id: parseInt(user["room-id"]!)
                    }
                });
                if (Services.Emote) {
                    await Services.Emote.syncAllEmotes(user["room-id"]!);
                }
                await Services.Symlinks.register(user["room-id"]!);
                return;
            }

            const prefix: string = (targetDb.prefix) ? targetDb.prefix : (globalTarget.prefix) ? globalTarget.prefix : "!";

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
                    ID: user["room-id"]!
                },
                Message: {
                    raw: message,
                    command: (message.startsWith(prefix)) ? message.split(prefix)[1].split(' ')[0] : ""
                }
            }

            // Assigning the roles:
            if (userDb !== null && userDb.int_role !== null) {
                args.Sender.intRole = userDb.int_role;
            }

            if (args.Target.ID === args.Sender.ID) args.Sender.extRole = IModule.AccessLevels.BROADCASTER;
            if (user["badges"]?.moderator === "1") args.Sender.extRole = IModule.AccessLevels.MOD;
            if (user["badges"]?.vip === "1") args.Sender.extRole = IModule.AccessLevels.VIP;

            // +1 chat line to the target's file:
            await Services.DB.target.update({
                where: {id: targetDb.id},
                data: {
                    chat_lines: (targetDb.chat_lines === null) ? 1 : targetDb.chat_lines + 1
                }
            });

            // +1 used times to the emote:
            if (Services.Emote !== undefined) await Services.Emote.increaseEmoteCount(args.Message.raw, args.Target.ID);

            // Complete a test:
            if (message == "test") {
                await Services.DB.target.update({
                    where: {id: targetDb.id},
                    data: {
                        tests: (targetDb.tests === null) ? 1 : targetDb.tests + 1
                    }
                });

                Services.Client.say(
                    `#${args.Target.Username}`,
                    await Services.Locale.parsedText("msg.test", args, [
                        "test",
                        targetDb.tests
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

        process.once("SIGHUP", async () => {
            await Services.Client.disconnect();
        });

        setInterval(async () => {
            if (Services.Emote === undefined) return;

            for (const target of await Services.DB.target.findMany()) {
                await Services.Emote.syncAllEmotes(target.alias_id.toString());
            }
        }, 30000);
        if (Services.Timer !== undefined) Services.Timer.tick(Services.Client);
        if (Services.Emote) await Services.Emote.subscribeTo7TVEventAPI();
    }

    async function StaticCommandHandler(args: IArguments) {
        var target: Target | null = await args.Services.DB.target.findFirst({
            where: {alias_id: parseInt(args.Target.ID)}
        });

        if (target === null) return;

        var cmd: CustomResponses | null = await args.Services.DB.customResponses.findFirst({
            where: {
                id: args.Message.raw.split(' ')[0],
                targetId: target.id
            }
        });

        if (cmd === null) return;
        if (cmd.value == false) return;
        
        args.Services.Client.say(`#${args.Target.Username}`, cmd.response);
    }
}

export default Messages;