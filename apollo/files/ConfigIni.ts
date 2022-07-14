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

import { readFileSync, WriteFileOptions, writeFileSync } from "fs";

namespace ConfigIni {
    export async function parse(file_path: string) {
        var file: string[] = readFileSync(file_path, {encoding: "utf-8"}).split("\n");
        var dict: any;
        dict = {};

        file = file
            .map(function(s) { return s.replace(/^\s*|\s*$/g, ""); })
            .filter(function(x) { return x; });

        var currentSection: string = "";

        file.forEach(async (value: string) => {
            // Skip the comments:
            if (value.startsWith(';')) return;

            // Remove comment from the line:
            value = value.split(/;.*$/).join(' ');

            // Section parse:
            if (value.startsWith('[') && (value.slice(value.length - 1, value.length) == "]")) {
                var section = value.slice(1, value.length - 1);

                currentSection = section;
                dict[currentSection] = {};

                return;
            }

            var keyvalue: string[] = value.split('=');
            keyvalue[0] = keyvalue[0].trim();
            keyvalue[1] = keyvalue[1].trim();

            if (keyvalue[1].startsWith('"') && (keyvalue[1].slice(keyvalue[1].length - 1, keyvalue[1].length) == '"')) {
                keyvalue[1] = keyvalue[1].slice(1, keyvalue[1].length - 1);
            }

            if (currentSection != "") {
                dict[currentSection][keyvalue[0].trim()] = keyvalue[1].trim()
                return;
            }

            dict[keyvalue[0]] = JSON.parse(keyvalue[1]);
        });

        return dict;
    }

    export async function pack(content: any, path: string, options?: WriteFileOptions) {
        var file_content: string = ``;

        var sections = Object.keys(content);
        
        sections.forEach(async (value: string) => {
    
            // Generate a comment:
            if (value.startsWith("==") && (value.slice(value.length - 2, value.length) == "==")) {
                file_content = file_content + `; ${content[value][value]}\n`;
                return;
            }

            // Generate a section:
            if (value.startsWith('#')) {
                
                var keys = Object.keys(content[value]);
                file_content = file_content + `[${value.slice(1, value.length)}]\n`;

                keys.forEach(async (key: string) => {
                
                    // Generate a comment:
                    if (key.startsWith("==") && (key.slice(key.length - 2, key.length) == "==")) {
                        file_content = file_content + `; ${content[value][key]}\n`;
                        return;
                    }
    
                    // Generate a line:
                    file_content = file_content + `${key} = ${content[value][key]}\n`;
                });

                return;
            }

            // Generate a line:
            file_content = file_content + `${value} = ${content[value]}\n`;
        });

        writeFileSync(path, file_content, options);
    }
}

export default ConfigIni;