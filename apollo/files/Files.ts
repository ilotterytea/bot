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

const log: Logger = new Logger({name: "files"});

namespace Files {
    export async function verifySystemIntergrity(folder?: string | undefined) {
        var fd: string = (folder == undefined) ? "local" : folder;

        if (!(existsSync(`${fd}`))) mkdirSync(`${fd}`); log.debug("Created a new folder in ", fd, "/");
        if (!(existsSync(`${fd}/logs`))) mkdirSync(`${fd}/logs`); log.debug("Created a new folder in ", fd, "/logs");
        if (!(existsSync(`${fd}/users`))) mkdirSync(`${fd}/users`); log.debug("Created a new folder in ", fd, "/users");
        if (!(existsSync(`${fd}/users/pub`))) mkdirSync(`${fd}/users/pub`); log.debug("Created a new folder in ", fd, "/users/pub");
        if (!(existsSync(`${fd}/users/logs`))) mkdirSync(`${fd}/users/logs`); log.debug("Created a new folder in ", fd, "/users/logs");
        
        if (!(existsSync(`${fd}/apollottv.db`))) {
            writeFileSync(`${fd}/apollottv.db`, "", {encoding: "utf-8"});
            await createSampleSQL(`${fd}/apollottv.db`);
        }
    }

    async function createSampleSQL(file_path: string) {
        sql.open({
            filename: file_path,
            driver: sql3.Database
        }).then((db) => {
            db.exec("CREATE TABLE \"join\" (extId primary int, asAuthorized bool)");
            db.exec("CREATE TABLE \"target\" (extId primary int, prefix varchar(255) null, langid varchar(8), successfullyTests int null, chatlines int null, executedcmds int null, isSuspended bool)");
            db.exec("CREATE TABLE \"users\" (extId primary int, role varchar(16))");
            db.exec("CREATE TABLE \"modules\" (extId int, name varchar(255), enabledOn varchar(255))");
            db.exec("CREATE TABLE \"emotes\" (extId int, name varchar(255), targetId int, provider varchar(5), count int)");
        });
    }
}

export default Files;