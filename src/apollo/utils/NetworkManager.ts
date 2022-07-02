// Copyright (C) 2022 ilotterytea
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

import axios from "axios";
import { existsSync, writeFileSync } from "fs";
import ApolloConfiguration from "../ApolloConfiguration";
import ApolloLogger from "./ApolloLogger";
import distLanguageFiles from "./Localization";

async function getAssets(file_path: string) {
    try {
        const langs = await (await axios.get(ApolloConfiguration.env.NET.API_GETLANG)).data;

        if (Object.keys(langs).length == 0) {
            ApolloLogger.warn("NetworkManager", "Received 0 languages. Can't continue the script!");
            return false;
        }
        
        if (!existsSync(file_path)) {
            ApolloLogger.warn("NetworkManager", "The folder ", file_path, " doesn't exist!");
            return false;
        }
        const formatted_langs = await distLanguageFiles(langs);

        Object.keys(formatted_langs).forEach(async (value: string) => {
            writeFileSync(`${file_path}/${value}.json`, JSON.stringify(formatted_langs[value], null, 2), {encoding: "utf-8"});
            ApolloLogger.debug("NetworkManager", "The language file ", value, " saved!");
        });

        ApolloLogger.debug("NetworkManager", "Successfully saved the language files!");
    } catch (err: any) {
        ApolloLogger.error("NetworkManager", "Something went wrong while getting assets: ", err);
    }
}

export default getAssets;