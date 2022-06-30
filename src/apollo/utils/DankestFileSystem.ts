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

import { existsSync, mkdirSync, readFileSync, writeFileSync } from "fs";
import ApolloLogger from "./ApolloLogger";

function checkDir(
    rootFolder: string
) {
    var config_templates = {
        v1: {
                version: "v1",
                join: {
                  asanonymous: [],
                  asclient: []
                },
                prefix: "!",
                emotes: {
                },
                roles: {
                  authority: [],
                  feelingspecial: [],
                  permabanned: []
                },
                stats: {
                  chat_lines: {
                  },
                  executed_commands: {
                  },
                  tests: {
                  }
                },
                preferred: {
                }
            },
        v2: {
            version: "v2"
        }
    };

    if (!existsSync(`${rootFolder}`)) { mkdirSync(`${rootFolder}`); ApolloLogger.debug("DankestFileSystem", `The folder "${rootFolder}" created!`); }
    if (!existsSync(`${rootFolder}/logs`)) { mkdirSync(`${rootFolder}/logs`); ApolloLogger.debug("DankestFileSystem", `The folder "${rootFolder}/logs" created!`); }
    if (!existsSync(`${rootFolder}/users`)) { mkdirSync(`${rootFolder}/users`); ApolloLogger.debug("DankestFileSystem", `The folder "${rootFolder}/users" created!`); }
    if (!existsSync(`${rootFolder}/users/pubdata`)) { mkdirSync(`${rootFolder}/users/pubdata`); ApolloLogger.debug("DankestFileSystem", `The folder "${rootFolder}/users/pubdata" created!`); }
    if (!existsSync(`${rootFolder}/users/msgdata`)) { mkdirSync(`${rootFolder}/users/msgdata`); ApolloLogger.debug("DankestFileSystem", `The folder "${rootFolder}/users/msgdata" created!`); }

    if (!existsSync(`${rootFolder}/storage.json`)) { writeFileSync(`${rootFolder}/storage.json`, JSON.stringify(config_templates.v2, null, 2), {encoding: "utf-8"}); ApolloLogger.debug("DankestFileSystem", `The file "${rootFolder}/storage.json" created!`); }
    
    if (JSON.parse(readFileSync(`${rootFolder}/storage.json`, {encoding: "utf-8"}))["version"] != "v2") {
        ApolloLogger.warn("DankestFileSystem", `It looks like your storage file is out of date or has an unsupported version 
        (${JSON.parse(readFileSync(`${rootFolder}/storage.json`, {encoding: "utf-8"}))["version"]}). 
        If you do not solve this problem, the process may be aborted.`);
    }
}

export default { checkDir };