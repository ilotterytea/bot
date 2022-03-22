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
    description: "",
    cooldownMs: 0,
    superUserOnly: false
}

/**
 * Run the command.
 * @param {*} client Client.
 * @param {*} target Target.
 * @param {*} user User.
 * @param {*} msg Message.
 * @param {*} args Arguments.
 */
 exports.run = async (client, target, user, msg, args = {
    emote_data: any,
    emote_updater: any
}) => {
    const emotes = JSON.parse(readFileSync(`./saved/emote_data.json`, {encoding: "utf-8"}));

    let items = Object.keys(emotes).map((key) => {
        return [key, emotes[key]]
    });

    items.sort((f, s) => {
        return s[1] - f[1]
    });

    let top_emotes = items.slice(0, 5);

    client.say(target, `Top 5 emotes by the total count of used times: ${top_emotes[0][0]} (${top_emotes[0][1]}), ${top_emotes[1][0]} (${top_emotes[1][1]}), ${top_emotes[2][0]} (${top_emotes[2][1]}), ${top_emotes[3][0]} (${top_emotes[3][1]}), ${top_emotes[4][0]} (${top_emotes[4][1]})`);
};