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

            if (args.channel != "ilotterytea" || args.channel != "fembajtea") return null;

            if (args.role == "su" && args.msg_args.length > 1) {
                const name = args.msg_args[1].toLowerCase();
                const user = await args.gql.getUserByName(name);
                
                if (!user) return await args.lang.TranslationKey("cmd.join.execute.not_found", args, [args.msg_args[1]]);

                if (!(args.storage.join.asclient.includes(user.id))) {
                    args.storage.join.asclient.push(user.id);
                } else {
                    return await args.lang.TranslationKey("cmd.join.execute.already_in", args, [user.name, user.id]);
                }
                
                args.apollo.client.join(`#${name}`);
                args.apollo.client.say(`#${name}`, `FeelsDankMan 👋 hi @${name}`);

                return await args.lang.TranslationKey("cmd.join.execute.success", args, [user.name, user.id]);
            }

            const user = await args.gql.getUserByName(args.user.username);
            if (!user) return await args.lang.TranslationKey("cmd.join.execute.not_found", args, [args.user.username]);

            if (!(args.storage.join.asclient.includes(user.id))) {
                args.storage.join.asclient.push(user.id);
            } else {
                return await args.lang.TranslationKey("cmd.join.execute.already_in", args, [user.name, user.id]);
            }

            args.apollo.client.join(`#${args.user.username}`);
            args.apollo.client.say(`#${args.user.username}`, `FeelsDankMan 👋 hi @${args.user.username}`);

            return await args.lang.TranslationKey("cmd.join.execute.success", args, [user.name, user.id]);
        }
    }
}

let inCooldown = [];