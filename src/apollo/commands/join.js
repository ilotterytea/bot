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

module.exports = {
    cooldownMs: 120000,
    permissions: ["pub"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
            inCooldown.push(args.user.username);
            setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);

            if ((args.channel != "fembajtea") && (args.channel != "ilotterytea")) return null;

            if (args.storage.join.asclient.includes(args.user["user-id"])) return await args.lang.ParsedText("cmd.join.exec.already_in", args.channel, args.user.username);
            
            args.apollo.client.join(`#${args.user.username}`);
            
            args.storage.join.asclient.push(args.user["user-id"]);

            return await args.lang.ParsedText("cmd.join.exec.response", args.channel, args.user.username, args.user.username, args.user["user-id"]);
        }
    }
}

let inCooldown = [];