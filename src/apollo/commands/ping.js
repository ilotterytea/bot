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
var os = require("node-os-utils");

module.exports = {
    cooldownMs: 5000,
    permissions: ["pub"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
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
            let text = await args.lang.getNotFilteredTranslationKey("cmd.ping.execute.success", args);

            inCooldown.push(args.user.username);
            setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);

            return text.replace("%uptime%", formatTime(process.uptime())).replace("%logonchannels%", args.apollo.options.channelsToJoin.length).replace("%tmi%", Math.floor(Math.round(pingms * 1000))).replace("%mem%", mem);
        }
    }
}

let inCooldown = [];