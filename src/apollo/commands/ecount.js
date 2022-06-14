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
    cooldownMs: 1500,
    permissions: ["pub"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
            if (args.msg_args.length == 1) {
                return await args.lang.TranslationKey("cmd.ecount.execute.notfull", args);
            }
    
            if (args.msg_args[1] in args.emotes[args.target.slice(1, args.target.length)]) {
                return await args.lang.TranslationKey("cmd.ecount.execute.success", args, [args.msg_args[1], args.emotes[args.target.slice(1, args.target.length)][args.msg_args[1]].toLocaleString()]);
            }
            inCooldown.push(args.user.username);
            setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);
            return null;
        }
    }
}

let inCooldown = [];