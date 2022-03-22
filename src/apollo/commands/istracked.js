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
 * pepeLaugh TeaTime EL NO SABE
 */
exports.help = {
    name: "Is tracked that user?",
    author: "ilotterytea",
    description: "Check the user in anonymous or client.",
    cooldownMs: 0,
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
    const msg_args = msg.trim().split(' ');
    let message = ``

    if (args.join.as_anonymous.includes(msg_args[1].toLowerCase())) {
        message = message += `by anonymous client`;
    }
    if (args.join.as_client.includes(msg_args[1].toLowerCase())) {
        message = message += (message != ``) ? `, bot client` : `by bot client`;
    }

    if (message == ``) {
        client.say(target, `@${user.username}, User ${msg_args[1]} isn't tracked.`);
        return;
    }

    client.say(target, `@${user.username}, User ${msg_args[1]} is tracked ${message}`);
    return;
};