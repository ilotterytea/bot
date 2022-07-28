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

import { Command } from "commander";
import packagejson from "../../package.json";

/**
 * An silly Command-Line Interface.
 * @returns Fully baked program.
 */
function CLI() {
    const Program = new Command("apollo");

    Program
        .name(packagejson.displayName)
        .description(packagejson.description)
        .version(packagejson.version);

    Program.option("--init", "Generates the necessary files to make the bot fully functional. Use this option if you just installed the bot.", false);
    Program.option("--debug", "Enter debug mode. For web server: disables SSL connection and starts server on port 8080. For bot: enables the debug from tmi.js client. Used to test functions on the local machine.", false);
    Program.option("--test-web-only", "Does not launch the bot at startup.", false);

    Program.parse(process.argv);
    return Program;
}

export default CLI;