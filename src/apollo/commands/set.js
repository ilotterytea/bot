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

const { readdirSync, existsSync } = require("fs");

module.exports = {
    cooldownMs: 10000,
    permissions: ["su", "br"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
            inCooldown.push(args.user.username);
            setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);

            if (args.msg_args.length < 2) return await args.lang.ParsedText("cmd.set.exec.usage", args.channel, args.user.username);
            switch (args.msg_args[1]) {
                // Set the channel language (e.g. !set lang en_us):
                case "lang":
                    var availablelangs = readdirSync("data/langs").join('').split(".json").join(' ');
                    
                    if (args.msg_args.length < 3) return await args.lang.ParsedText("cmd.set.exec.languagesetup.available", args.channel, args.user.username, availablelangs);

                    if (existsSync(`data/langs/${args.msg_args[2].toLowerCase()}.json`)) {
                        if (!(args.channel.toLowerCase() in args.storage.preferred)) args.storage.preferred[args.channel.toLowerCase()] = {};
                        args.storage.preferred[args.channel.toLowerCase()].lang = args.msg_args[2].toLowerCase();

                        await args.lang.UpdateUserPreferrences();
                    } else {
                        return await args.lang.ParsedText("cmd.set.exec.languagesetup.available", args.channel, args.user.username, availablelangs);
                    }

                    return await args.lang.ParsedText("cmd.set.exec.languagesetup.response", args.channel, args.user.username);
                default:
                    return await args.lang.ParsedText("cmd.set.exec.usage", args.channel, args.user.username);
            }
        }
    }
}

let inCooldown = [];