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

const { default: axios } = require("axios")

module.exports = {
    cooldownMs: 5000,
    permissions: ["br"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
            var chatters = await (await axios.get(`https://tmi.twitch.tv/group/user/${args.target.slice(1, args.target.length)}/chatters`)).data.chatters;
            var msga = args.msg_args.filter(n => n !== `${args.prefix}massping`).join(' ');

            for (let i = 0; i < chatters.broadcaster.length; i++) {
                args.apollo.client.say(args.target, `@${chatters.broadcaster[i]}, ${msga}`);
            }

            for (let i = 0; i < chatters.vips.length; i++) {
                args.apollo.client.say(args.target, `@${chatters.vips[i]}, ${msga}`);
            }

            for (let i = 0; i < chatters.moderators.length; i++) {
                args.apollo.client.say(args.target, `@${chatters.moderators[i]}, ${msga}`);
            }

            for (let i = 0; i < chatters.staff.length; i++) {
                args.apollo.client.say(args.target, `@${chatters.staff[i]}, ${msga}`);
            }

            for (let i = 0; i < chatters.admins.length; i++) {
                args.apollo.client.say(args.target, `@${chatters.admins[i]}, ${msga}`);
            }

            for (let i = 0; i < chatters.global_mods.length; i++) {
                args.apollo.client.say(args.target, `@${chatters.global_mods[i]}, ${msga}`);
            }

            for (let i = 0; i < chatters.viewers.length; i++) {
                args.apollo.client.say(args.target, `@${chatters.viewers[i]}, ${msga}`);
            }
            inCooldown.push(args.user.username);
            setTimeout(() => inCooldown = inCooldown.filter(u => u !== args.user.username), this.cooldownMs);
            return null;
        }
    }
}

let inCooldown = [];