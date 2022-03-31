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

/**
 * Help.
 */
exports.help = {
    value: true,
    name: "7TV channel emote count!",
    author: "ilotterytea",
    description: "Shows how much of the specified 7TV channel emote has been used. Updates the emote database every 90 seconds.",
    cooldownMs: 1500,
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
        const mArgs = msg.split(' ');
        //const username = (msg.split(' ').length > 2 && msg.includes("-e:")) ? ... : null

        if (mArgs.length == 1) {
            client.say(target, `@${user.username}, provide an emote.`);
            return;
        };
        
        if (mArgs[1] in args.emote_data[target.slice(1, target.length)]["stv"]) {
            client.say(target, `${mArgs[1]} has been used ${args.emote_data[target.slice(1, target.length)]["stv"][mArgs[1]].toLocaleString()} times.`);
            return;
        } else {
            client.say(target, `@${user.username}, I know nothing about ${mArgs[1]} ¯\\_ FeelsDankMan _/¯ (Advice: use !help ecount for more info haHAA )`);
        }

        inCooldown.push(user.username);
        setTimeout(() => inCooldown = inCooldown.filter(u => u !== user.username), this.help.cooldownMs);
    }
};

let inCooldown = [];