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
const { readFileSync, writeFileSync } = require("fs");
const tmi = require("tmi.js");

/**
 * Help.
 */
module.exports.help = {
    name: "Turn off the bot!",
    author: "ilotterytea",
    description: "Turn off the bot with a command. Used during the bot test on the local computer.",
    cooldownMs: 0,
    superUserOnly: false,
    authorOnly: true
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
    client.say(target, "peepoLeave ");
    writeFileSync("./saved/emotes.json", JSON.stringify(args.emote_data, null, 2), {encoding: "utf-8"});
    console.log(`* Emote file saved!`);
    client.disconnect();
    console.log(`!!! ${user.username} has shut down the bot`);
};

let inCooldown = [];