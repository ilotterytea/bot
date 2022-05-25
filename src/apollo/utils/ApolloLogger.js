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

const { appendFileSync } = require("fs")

module.exports = function (message, level, showInConsole = false) {
    function pad(e) {
        return ( (e < 10) ? "0" : "" ) + e.toString();
    }
    const date = new Date();
    const date_minimal = `${pad(date.getUTCDate())}-${pad(date.getUTCMonth() + 1)}-${pad(date.getUTCFullYear())}`
    const date_template = `${pad(date.getUTCDate())}-${pad(date.getUTCMonth() + 1)}-${pad(date.getUTCFullYear())} ${pad(date.getUTCHours())}:${pad(date.getUTCMinutes())}:${pad(date.getUTCSeconds())}.${pad(date.getUTCMilliseconds())}`;
    appendFileSync(`storage/logs/${date_minimal}.log`, `[${date_template} (UTC)] ${(level == "log") ? "*" : "!!!"} ${message}\n`, {encoding: "utf-8"});
    if (showInConsole) {
        console.log(`[${date_template} (UTC)] ${(level == "log") ? "*" : "!!!"} ${message}`);
    }
}