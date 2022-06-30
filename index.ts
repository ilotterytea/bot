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

import chalk from "chalk";
import { Command } from "commander";
import ApolloClient from "./src/apollo/clients/ApolloClient";
import pck from "./package.json";
import ApolloConfiguration from "./src/apollo/ApolloConfiguration";
import TwitchAPI from "./src/apollo/clients/TwitchAPI";
import MessageHandler from "./src/apollo/handlers/MessageHandler";
import ApolloLogger from "./src/apollo/utils/ApolloLogger";

import {version, name} from "./package.json";

main();

async function main() {
    ApolloLogger.debug("Initializer", `--> Hello, user! I'm using ${name} v${version}!`);
    const CLIOptions = CLI();
    const isInDebugMode = CLIOptions.debug;
    const disableInternetConnection = CLIOptions["disable-internet-sync"];

    const API = await TwitchAPI(ApolloConfiguration.Passport.ClientID, ApolloConfiguration.Passport.AccessToken);
    const Apollo = ApolloClient(ApolloConfiguration.Passport.Username, ApolloConfiguration.Passport.Password, API, isInDebugMode);

    const MsgHandler = new MessageHandler(Apollo, API);
    try {
        MsgHandler.HandleMessages();
        MsgHandler.SubEvents();
    } catch (err) {
        ApolloLogger.error("Initializer", "Something went wrong while handling the messages: ", err);
    }
}

/**
 * A lidl Command-Line Interface.
 * @returns dictionary with booleans.
 */
function CLI() {
    const Program = new Command();

    // Some program metadata:
    Program
        .name(pck.displayName)
        .description(pck.description)
        .version(pck.version);

    // Debug mode:
    Program
        .option("--debug", "Enables the debug mode.", false)
        .alias("-d");
    
    // Semi-autonomous mode:
    Program
        .option("--disable-internet-sync", "Don't update localization files every time you reboot.", false)
        .alias("-i");
    
    Program
        .option("--console-mode", "Connecting to the Twitch, but commands will be accepted in terminal")
    
    // Parse the arguments:
    Program.parse(process.argv);

    // Return the boolean of options:
    return Program.opts();
}