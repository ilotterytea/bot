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

// Libraries:
const { appendFileSync } = require("fs");

/**
 * Apollo Logger.
 * @param {*} log_level Log level. There are 3 log levels: debug, warning, error.
 * @param {*} name Script name. This parameter will be removed when I make a good logger.
 * @param  {...any} args Debug message.
 */
function ApolloLogger(log_level, name, ...args) {
    function pad(e) {
        return ( (e < 10) ? "0" : "" ) + e.toString();
    }

    const date = new Date();
    const date_minimal = `${pad(date.getUTCDate())}-${pad(date.getUTCMonth() + 1)}-${pad(date.getUTCFullYear())}`
    const date_template = `${pad(date.getUTCDate())}-${pad(date.getUTCMonth() + 1)}-${pad(date.getUTCFullYear())} ${pad(date.getUTCHours())}:${pad(date.getUTCMinutes())}:${pad(date.getUTCSeconds())}.${pad(date.getUTCMilliseconds())}`;

    switch (log_level) {
        
        case "debug":
            appendFileSync(`./storage/logs/${date_minimal}.log`, `[${date_template}] [DEBUG] ${name.toUpperCase()}: ${args.join(' ')}\n`);
            console.debug(`${"[" + date_template + "]"} ${"[DEBUG]"} ${name.toUpperCase()}: ${args.join(' ')}`);
            break;
        case "warn":
            appendFileSync(`./storage/logs/${date_minimal}.log`, `[${date_template}] [WARN] ${name.toUpperCase()}: ${args.join(' ')}\n`);
            console.debug(`${"[" + date_template + "]"} ${"[WARN]"} ${name.toUpperCase()}: ${args.join(' ')}`);
            break;
        case "error":
            appendFileSync(`./storage/logs/${date_minimal}.log`, `[${date_template}] [ERROR] ${name.toUpperCase()}: ${args.join(' ')}\n`);
            console.debug(`${"[" + date_template + "]"} ${"[ERROR]"} ${name.toUpperCase()}: ${args.join(' ')}`);
            break;
    }
}

/**
 * Log with debug level.
 * @param {*} name Script name. This parameter will be removed when I make a good logger.
 * @param  {...any} args Debug message.
 */
function debug(name, ...args) {
    ApolloLogger("debug", name, ...args);
};

/**
 * Log with warn level.
 * @param {*} name Script name. This parameter will be removed when I make a good logger.
 * @param  {...any} args Debug message.
 */
function warn(name, ...args) {
    ApolloLogger("warn", name, ...args);
};

/**
 * Log with error level.
 * @param {*} name Script name. This parameter will be removed when I make a good logger.
 * @param  {...any} args Debug message.
 */
function error(name, ...args) {
    ApolloLogger("error", name, ...args);
};

module.exports = {debug, warn, error};