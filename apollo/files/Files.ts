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

import { existsSync, mkdirSync, writeFileSync } from "fs";
import { Logger } from "tslog";
import sql3 from "sqlite3";
import sql from "sqlite";
import IStorage from "../interfaces/IStorage";

const log: Logger = new Logger({name: "files"});

namespace Files {
    export async function verifySystemIntergrity(folder?: string | undefined) {
        var fd: string = (folder == undefined) ? "local" : folder;

        if (!(existsSync(`${fd}`))) {
            mkdirSync(`${fd}`);
            log.debug("Created a new folder", `${fd}`);
        }

        if (!(existsSync(`${fd}/logs`))) {
            mkdirSync(`${fd}/logs`);
            log.debug("Created a new folder", `${fd}/logs`);
        }

        if (!(existsSync(`${fd}/targets`))) {
            mkdirSync(`${fd}/targets`);
            log.debug("Created a new folder", `${fd}/targets`);
        }

        if (!(existsSync(`${fd}/datastore.json`))) {
            generateANewStorageFile(`${fd}/datastore.json`);
            log.debug("Created a new file", `${fd}/datastore.json`);
        }

        if (!(existsSync("config.ini"))) {
            generateANewCfgFile("config.ini");
            log.debug("Created a new file", `${fd}/config.ini`);
        }
    }

    function generateANewStorageFile(file_path: string) {
        var data: IStorage.Main = {
            Version: "v2",
            Join: {
                AsClient: [],
                AsAnonymous: []
            },
            Global: {
                Prefix: "!",
                Modules: [],
                Users: []
            }
        }

        writeFileSync(file_path, JSON.stringify(data, null, 2), {encoding: "utf-8"});
    }

    function generateANewCfgFile(file_path: string) {
        var text: string = `[Authorization]\nUsername = ""\nPassword = ""\nClientID = ""\nAccessToken = ""`;
        writeFileSync(file_path, text, {encoding: "utf-8"});
    }
}

export default Files;