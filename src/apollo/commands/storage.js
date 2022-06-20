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
    permissions: ["su"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
            inCooldown.push(args.user.username);
            setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);

            var edit = args.msg_args[1]; // chown, prefix, part

            // Change the user group (e.g. !storage chown user:group):
            if (edit == "chown") {
                if (args.msg_args.length < 3) return null;

                var ur = args.msg_args[2].split(':');
                var user = await args.gql.getUserByName(ur[0]);

                if (!user) return await args.lang.ParsedText("user.not_found", args.channel, args.user.username, ur[0]);

                switch (ur[1]) {
                    case "authority":
                        args.storage.roles.feelingspecial       = args.storage.roles.feelingspecial.filter(u => u !== user.id);
                        args.storage.roles.permabanned          = args.storage.roles.permabanned.filter(u => u !== user.id);
                        args.storage.roles.authority.push(user.id);
                        break;
                    case "special":
                        args.storage.roles.authority            = args.storage.roles.authority.filter(u => u !== user.id);
                        args.storage.roles.permabanned          = args.storage.roles.permabanned.filter(u => u !== user.id);
                        args.storage.roles.feelingspecial.push(user.id);
                        break;
                    case "ban":
                        args.storage.roles.authority            = args.storage.roles.authority.filter(u => u !== user.id);
                        args.storage.roles.feelingspecial       = args.storage.roles.feelingspecial.filter(u => u !== user.id);
                        args.storage.roles.permabanned.push(user.id);
                        break;
                    case "user":
                        args.storage.roles.authority            = args.storage.roles.authority.filter(u => u !== user.id);
                        args.storage.roles.feelingspecial       = args.storage.roles.feelingspecial.filter(u => u !== user.id);
                        args.storage.roles.permabanned          = args.storage.roles.permabanned.filter(u => u !== user.id);
                        break;
                    default:
                        return await args.lang.ParsedText("cmd.storage.exec.group.not_found", args.channel, args.user.username, ur[1]);
                }

                return await args.lang.ParsedText("cmd.storage.exec.group.changed", args.channel, args.user.username, user.name, user.id, ur[1]);
            }

            // Change the prefix (e.g. !storage prefix ~@):
            if (edit == "prefix") {
                if (args.msg_args.length < 3) return await args.lang.ParsedText("cmd.storage.exec.prefix.now", args.channel, args.storage.prefix);

                var pref = args.msg_args[2];
                args.storage.prefix = pref;

                return await args.lang.ParsedText("cmd.storage.exec.prefix.changed", args.channel, args.user.username, pref);
            }

            // Leave the channel (e.g. !storage part monkeos):
            if (edit == "part") {
                if (args.msg_args.length < 3) return null;

                var user = await args.gql.getUserByName(args.msg_args[2]);

                if (!user) return await args.lang.ParsedText("user.not_found", args.channel, args.user.username, args.msg_args[2]);
                if (args.storage.join.asclient.includes(user.id)) {
                    args.storage.join.asclient = args.storage.join.asclient.filter(u => u !== user.id);
                    args.apollo.client.part(`#${user.name}`);
                    
                    return await args.lang.ParsedText("cmd.storage.exec.part.successfully", args.channel, args.user.username, user.name, user.id);
                } else {
                    return await args.lang.ParsedText("cmd.storage.exec.part.not_in_join_list", args.channel, args.user.username, user.name, user.id);
                }
            }
        }
    }
}

let inCooldown = [];