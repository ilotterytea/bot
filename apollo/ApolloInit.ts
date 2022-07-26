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

import { Client } from "tmi.js";
import { Logger } from "tslog";
import TwitchApi from "./clients/ApiClient";
import ApolloClient from "./clients/ApolloClient";
import ConfigIni from "./files/ConfigIni";
import StoreManager from "./files/StoreManager";
import Messages from "./handlers/MessageHandler";
import IConfiguration from "./interfaces/IConfiguration";
import CLI from "./utils/CLI";
import EmoteUpdater from "./utils/emotes/EmoteUpdater";
import Localizator from "./utils/Locale";
import ModuleManager from "./utils/ModuleManager";

const log: Logger = new Logger({name: "itb2-main"});

async function ApolloInit(opts: {[key: string]: any}) {
    const Config: IConfiguration = await ConfigIni.parse("config.ini");
    const Datastore: StoreManager = new StoreManager("local/datastore.json", "local/targets");
    const Locale: Localizator = new Localizator();
    const Modules: ModuleManager = new ModuleManager();
    const STVEmotes: EmoteUpdater.SevenTV = new EmoteUpdater.SevenTV(["ilotterytea", "fembajbot"]);

    await Locale.loadLanguages(false);
    Locale.setPreferredLanguages(Datastore.targets.getTargets, Datastore.targets.getUserlinks());
    Modules.init();

    const TmiApi: TwitchApi.Client = new TwitchApi.Client(
        Config.Authorization.ClientID,
        Config.Authorization.AccessToken
    );

    const TmiClient: Client = ApolloClient(
        Config.Authorization.Username,
        Config.Authorization.Password,
        Datastore.getClientChannels!,
        TmiApi,
        opts["debug"]
    );

    try {
        await Messages.Handler(TmiClient, TmiApi, Datastore, Locale, Modules, STVEmotes);

    } catch (err) {
        log.error(err);
    }

}

export default ApolloInit;