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
const { readFileSync } = require("fs");
const tmi = require("tmi.js");

/**
 * Help.
 */
module.exports.help = {
    name: "Emote top!",
    author: "ilotterytea",
    description: "Top 5 most used 7TV channel emotes.",
    cooldownMs: 5000,
    superUserOnly: false,
    authorOnly: false
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
        let items = Object.keys(args.emote_data[target.slice(1, target.length)]["stv"]).map((key) => {
            return [key, args.emote_data[target.slice(1, target.length)]["stv"][key]]
        });

        items.sort((f, s) => {
            return s[1] - f[1]
        });

        let top_emotes = items.slice(0, 5);

        client.say(target, `Top 5 emotes by the total count of used times: ${top_emotes[0][0]} (${top_emotes[0][1]}), ${top_emotes[1][0]} (${top_emotes[1][1]}), ${top_emotes[2][0]} (${top_emotes[2][1]}), ${top_emotes[3][0]} (${top_emotes[3][1]}), ${top_emotes[4][0]} (${top_emotes[4][1]})`);
        
        inCooldown.push(user.username);
        setTimeout(() => inCooldown = inCooldown.filter(u => u !== user.username), this.help.cooldownMs);
    }
};

let inCooldown = [];