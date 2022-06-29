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
const { readFileSync } = require("fs");
var os = require("node-os-utils");
const { version, name } = require("../../../package.json");

module.exports = {
    cooldownMs: 5000,
    permissions: ["pub"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
            inCooldown.push(args.user.username);
            setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);

            var mem = `${Math.round(await (await os.mem.used()).usedMemMb)} MB/${Math.round(await (await os.mem.used()).totalMemMb)} MB`

            function formatTime(seconds) {
                function pad(s) {
                    return (s < 10 ? '0': '') + s;
                }

                var days = Math.floor(seconds / (60 * 60 * 24));
                var hours = Math.floor(seconds / (60 * 60) % 24);
                var minutes = Math.floor(seconds % (60 * 60) / 60);
                var sec = Math.floor(seconds % 60);

                return `${days} d. ${pad(hours)}:${pad(minutes)}:${pad(sec)}`;
            }

            var pingms = await args.apollo.client.ping();

            var uptime = formatTime(process.uptime());
            var logon_rooms = args.apollo.options.channelsToJoin.length;
            var tmiping = Math.floor(Math.round(pingms * 1000));

            var commit = readFileSync("storage/commit.txt", {encoding: "utf-8"}).split("-0-g");
            var commit_branch = commit[0].replace("heads/", "");

            return await args.lang.ParsedText("cmd.ping.exec.response", args.channel, "FeelsDankMan 🏓 ", uptime, logon_rooms, mem, tmiping, `${version}-${name}`, commit[1], commit_branch);
        }
    }
}

let inCooldown = [];