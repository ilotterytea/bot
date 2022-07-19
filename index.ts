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

import { Logger } from "tslog";
import ApolloInit from "./apollo/ApolloInit";
import Files from "./apollo/files/Files";
import CLI from "./apollo/utils/CLI";
import ServerInit from "./www/ServerInit";

const log: Logger = new Logger({name: "itb2-main"});

async function Main() {
    log.silly("Loading, please wait...");
    const CLIArguments = CLI().opts();

    if (CLIArguments["init"]) {
        log.silly("Initializating first setup...");

        await Files.verifySystemIntergrity("./local");

        return;
    }
    await ServerInit(CLIArguments);
    if (!CLIArguments["testWebOnly"]) await ApolloInit(CLIArguments);
}

Main();

process.on("unhandledRejection", (reason: unknown, promise: Promise<unknown>) => {
    log.error(reason);
});

process.on("uncaughtException", (error: Error, origin: NodeJS.UncaughtExceptionListener) => {
    log.fatal(error);
});