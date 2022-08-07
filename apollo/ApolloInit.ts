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

import EmoteLib from "emotelib";
import { Client } from "tmi.js";
import { Logger } from "tslog";
import TwitchApi from "./clients/ApiClient";
import ApolloClient from "./clients/ApolloClient";
import ConfigIni from "./files/ConfigIni";
import LocalStorage from "./files/LocalStorage";
import Messages from "./handlers/MessageHandler";
import StaticCommandHandler from "./handlers/StaticCommandHandler";
import TimerHandler from "./handlers/TimerHandler";
import IConfiguration from "./interfaces/IConfiguration";
import EmoteUpdater from "./utils/emotes/EmoteUpdater";
import Localizator from "./utils/Locale";
import ModuleManager from "./utils/ModuleManager";
const log: Logger = new Logger({name: "itb2-main"});

async function ApolloInit(
    Opts: {[key: string]: any},
    Storage: LocalStorage,
    TmiApi: TwitchApi.Client, 
    Config: IConfiguration
) {
    const Locale: Localizator = new Localizator();

    Locale.load("localization/bot.json");

    const Modules: ModuleManager = new ModuleManager();
    const Emotelib: EmoteLib = new EmoteLib({
        client_id: Config.Authorization.ClientID,
        access_token: Config.Authorization.AccessToken
    });

    const Timer: TimerHandler = new TimerHandler(Storage.Targets.getTargets);
    await Timer.IDsToUsernames(TmiApi);

    Locale.setPreferredLanguages(Storage.Targets.getTargets, Storage.Global.getSymlinks);

    Modules.init();

    const TmiClient: Client = ApolloClient(
        Config.Authorization.Username,
        Config.Authorization.Password,
        Object.keys(Storage.Global.getSymlinks),
        Opts["debug"]
    );

    const Emotes: EmoteUpdater = new EmoteUpdater({
        identify: {
            access_token: Config.Authorization.AccessToken,
            client_id: Config.Authorization.ClientID
        },
        services: {
            client: TmiClient,
            localizator: Locale,
            twitch_api: TmiApi
        }
    });

    await Emotes.load(Storage.Targets.getTargets);
    await Emotes.subscribeTo7TVEventAPI();

    const StaticCommands: StaticCommandHandler = new StaticCommandHandler(Storage.Targets.getTargets);

    try {
        for (const name of Object.keys(Storage.Global.getSymlinks)) {
            await Emotes.sync7TVEmotes(name, false);
            await Emotes.syncBTTVEmotes(name, false);
            await Emotes.syncFFZEmotes(name, false);
            await Emotes.syncTTVEmotes(name, false);
        }

        await Messages.Handler({
            Client: TmiClient,
            Locale: Locale,
            Storage: Storage,
            TwitchApi: TmiApi,
            Timer: Timer,
            Module: Modules,
            Emote: Emotes,
            StaticCmd: StaticCommands
        });        

    } catch (err) {
        log.error(err);
    }
}

export default ApolloInit;