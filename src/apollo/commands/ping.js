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
    name: "Ping!",
    author: "ilotterytea",
    description: "Checking if it's alive, and a bunch of other data.",
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
    function timeformat(seconds){
        function pad(s){
            return (s < 10 ? '0' : '') + s;
        }
        var days = Math.floor(seconds / (60*60*24))
        var hours = Math.floor(seconds / (60 * 60) % 24);
        var minutes = Math.floor(seconds % (60*60) / 60);
        var sec = Math.floor(seconds % 60); 
    
        
        return `${days}d. ${pad(hours)}:${pad(minutes)}:${pad(sec)}`;
    }

    const emotes = JSON.parse(readFileSync(`./saved/emote_data.json`, {encoding: "utf-8"}));

    let items = Object.keys(emotes).map((key) => {
        return [key, emotes[key]]
    });

    items.sort((f, s) => {
        return s[1] - f[1]
    });

    let top_emotes = items.slice(0, 5);

    client.ping().then((ms) => {
        client.say(target, `@${user.username}, Pong! Session uptime: ${timeformat(process.uptime())}! ${args.join.as_anonymous.length} channels are tracked in anonymous mode, landed in ${args.join.as_client.length} channels. Latency to TMI: ${ms}s forsenLevel`);
    });
};