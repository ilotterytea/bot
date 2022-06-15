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
    cooldownMs: 5000,
    permissions: ["pub"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
            if (args.msg_args.length == 2 && Object.keys(args.emotes).includes(args.msg_args[1].toLowerCase())) {
                let items = Object.keys(args.emotes[args.msg_args[1].toLowerCase()]).map((key) => {
                    return [key, args.emotes[args.msg_args[1].toLowerCase()][key]]
                });
                items.sort((f, s) => {
                    return s[1] - f[1]
                });
        
                let top_emotes = items.slice(0, 15);
        
                console.log(top_emotes)

                inCooldown.push(args.user.username);
                setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);
        
                return `${await args.lang.TranslationKey("cmd.etop.execute.success", args)} ${top_emotes[0][0]} (${top_emotes[0][1]}), ${top_emotes[1][0]} (${top_emotes[1][1]}), ${top_emotes[2][0]} (${top_emotes[2][1]}), ${top_emotes[3][0]} (${top_emotes[3][1]}), ${top_emotes[4][0]} (${top_emotes[4][1]}), ${top_emotes[5][0]} (${top_emotes[5][1]}), ${top_emotes[6][0]} (${top_emotes[6][1]}), ${top_emotes[7][0]} (${top_emotes[7][1]}), ${top_emotes[8][0]} (${top_emotes[8][1]}), ${top_emotes[9][0]} (${top_emotes[9][1]}), ${top_emotes[10][0]} (${top_emotes[10][1]}), ${top_emotes[11][0]} (${top_emotes[11][1]}), ${top_emotes[12][0]} (${top_emotes[12][1]}), ${top_emotes[13][0]} (${top_emotes[13][1]}), ${top_emotes[14][0]} (${top_emotes[14][1]})`;
            } else {
                let items = Object.keys(args.emotes[args.target.slice(1, args.target.length)]).map((key) => {
                    return [key, args.emotes[args.target.slice(1, args.target.length)][key]]
                });
                items.sort((f, s) => {
                    return s[1] - f[1]
                });
        
                let top_emotes = items.slice(0, 15);

                inCooldown.push(args.user.username);
                setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);
        
                return `${await args.lang.TranslationKey("cmd.etop.execute.success", args)} ${top_emotes[0][0]} (${top_emotes[0][1]}), ${top_emotes[1][0]} (${top_emotes[1][1]}), ${top_emotes[2][0]} (${top_emotes[2][1]}), ${top_emotes[3][0]} (${top_emotes[3][1]}), ${top_emotes[4][0]} (${top_emotes[4][1]}), ${top_emotes[5][0]} (${top_emotes[5][1]}), ${top_emotes[6][0]} (${top_emotes[6][1]}), ${top_emotes[7][0]} (${top_emotes[7][1]}), ${top_emotes[8][0]} (${top_emotes[8][1]}), ${top_emotes[9][0]} (${top_emotes[9][1]}), ${top_emotes[10][0]} (${top_emotes[10][1]}), ${top_emotes[11][0]} (${top_emotes[11][1]}), ${top_emotes[12][0]} (${top_emotes[12][1]}), ${top_emotes[13][0]} (${top_emotes[13][1]}), ${top_emotes[14][0]} (${top_emotes[14][1]})`;
            }
        }
    }
}

let inCooldown = [];