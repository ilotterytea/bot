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
const emoteupdate = require("../../utils/SevenTVEmoteUpdater")

/**
 * Help.
 */
exports.help = {
    value: true,
    name: "Emote Update!",
    author: "ilotterytea",
    description: "Shows how much of the specified emote has been used.",
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
    try {
        args.emote_updater.updateEmote();
        client.say(target, `Emotes has been successfully updated! :)`);
    } catch (err) {
        console.log(err);
        client.say(target, `Something went wrong NotLikeThis`);
    }
};