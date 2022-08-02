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
import StoreManager from "./files/StoreManager";
import Messages from "./handlers/MessageHandler";
import IConfiguration from "./interfaces/IConfiguration";
import EmoteUpdater from "./utils/emotes/EmoteUpdater";
import Localizator from "./utils/Locale";
import ModuleManager from "./utils/ModuleManager";
const log: Logger = new Logger({name: "itb2-main"});

async function ApolloInit(opts: {[key: string]: any}, Datastore: StoreManager, TmiApi: TwitchApi.Client, cfg: IConfiguration) {
    const Config: IConfiguration = cfg;

    await Datastore.parseChannels(Datastore.getClientChannelIDs);

    const Locale: Localizator = new Localizator();

    Locale.load("localization/bot.json");

    const Modules: ModuleManager = new ModuleManager();
    const Emotelib: EmoteLib = new EmoteLib({
        client_id: Config.Authorization.ClientID,
        access_token: Config.Authorization.AccessToken
    });

    Locale.setPreferredLanguages(Datastore.targets.getTargets, Datastore.targets.getUserlinks());
    Modules.init();

    const TmiClient: Client = ApolloClient(
        Config.Authorization.Username,
        Config.Authorization.Password,
        Datastore.getClientChannelNames!,
        opts["debug"]
    );

    const STVEmotes: EmoteUpdater.SevenTV = new EmoteUpdater.SevenTV(Emotelib.seventv, Datastore.getClientChannelNames);

    await STVEmotes.load(Datastore.targets.getTargets);

    STVEmotes.subscribeToEmoteUpdates(TmiClient, Locale, Datastore.getClientChannelNames);
    try {
        await Messages.Handler(TmiClient, TmiApi, Datastore, Locale, Modules, STVEmotes);
    } catch (err) {
        log.error(err);
    }

}

export default ApolloInit;