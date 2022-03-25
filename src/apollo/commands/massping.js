// Copyright (C) 2022 NotDankEnough (iLotterytea)
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

// Libraries.
const {
    readFileSync
} = require("fs");
const tmi = require("tmi.js");
const axios = require("axios").default;

/**
 * Help.
 */
module.exports.help = {
    name: "Ping em, Fors! LUL 💪 ",
    author: "ilotterytea",
    description: "",
    cooldownMs: 15000,
    superUserOnly: true
}

/**
 * Run the command.
 * @param {*} client Client.
 * @param {*} target Target.
 * @param {*} user User.
 * @param {*} msg Message.
 * @param {*} args Arguments.
 */
exports.run = async (client, target, user, msg, args) => {
    if (!inCooldown.includes(user.username)) {
        const users = await (await axios.get(`https://tmi.twitch.tv/group/user/${target.slice(1, target.length)}/chatters`)).data.chatters;
        let args = msg.trim().split(' ');

        // -o: Ping in one message:
        if (args.includes("-o")) {
            let users_string = "";

            for (let i = 0; i < users.moderators.length; i++) {
                users_string = users_string += `${users.moderators[i]} `
            }
            for (let i = 0; i < users.vips.length; i++) {
                users_string = users_string += `${users.vips[i]} `
            }
            for (let i = 0; i < users.viewers.length; i++) {
                users_string = users_string += `${users.viewers[i]} `
            }

            args = args.filter(u => u !== "-o");
            args = args.filter(u => u !== "!massping");

            client.say(target, `${users_string} ${args.join(' ')}`);

            inCooldown.push(user.username);
            setTimeout(() => inCooldown = inCooldown.filter(u => u !== user.username), this.help.cooldownMs);
            return;
        }
        args = args.filter(u => u !== "!massping");

        for (let i = 0; i < users.moderators.length; i++) {
            client.say(target, `@${users.moderators[i]}, ${args.join(' ')}`);
        }
        for (let i = 0; i < users.vips.length; i++) {
            client.say(target, `@${users.vips[i]}, ${args.join(' ')}`);
        }
        for (let i = 0; i < users.viewers.length; i++) {
            client.say(target, `@${users.viewers[i]}, ${args.join(' ')}`);
        }

        inCooldown.push(user.username);
        setTimeout(() => inCooldown = inCooldown.filter(u => u !== user.username), this.help.cooldownMs);
    }
};

let inCooldown = [];