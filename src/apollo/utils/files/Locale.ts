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

import axios, { AxiosResponse } from "axios";
import { writeFileSync } from "fs";
import SillyLogger from "../ApolloLogger";

namespace Locale {
    export class Localizator {
        constructor() {

        }

        async parsedText(line_id: string, channel_id: string | undefined, ...args: string[]) {
            return "";
        }
    }

    export async function getLaunguageFiles() {
        SillyLogger.debug("Locale", "Getting the language files...");
        const file: AxiosResponse<any, any> = await axios.get("https://silly.hmmtodayiwill.ru/v1/getBotLanguages", {responseType: "json"});

        if (file.status != 200) {
            return SillyLogger.error("Locale", "The API server isn't reachable.");
        }

        const availableLangs: string[] = file.data["#availableLangs"];
        SillyLogger.debug("Locale", "Available languages:", availableLangs.join(','));

        
    }

    async function buildLanguageFiles(available_langs: string[], raw_json: any) {
        SillyLogger.debug("Locale", "Starting to build the language files...");

        const translationKeys = Object.keys(raw_json);
        var dict: any;

        dict = {};

        available_langs.forEach(async (lang: string) => {
            if (!(lang in dict)) dict[lang] = {};
            
            translationKeys.forEach( (value: string) => {
                if (value.startsWith("#")) return null;
                SillyLogger.debug("Locale", `Packing up (${lang}.${value})...`);

                dict[lang][value] = raw_json[value][lang];
            });
        });

        Object.keys(dict).forEach(async (value: string) => {
            writeFileSync(`./local/lang/${value}.json`, JSON.stringify(dict[value], null, 2), {encoding: "utf-8"});
            SillyLogger.debug("Locale", `Packed up the ${value} language file with ${Object.keys(dict[value]).length} lines!`);
        });

        SillyLogger.debug("Locale", "Finished to building the language files!");
    }
}

export default Locale;