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
    permissions: ["su", "br"],
    execute: async (args) => {
        if (!inCooldown.includes(args.user.username)) {
            var option = args.msg_args[1];

            switch (option) {
                case "list":
                    var cmds = args.staticcmd.getAllStaticCommands(args.user["room-id"]);
                    if (!cmds) return await args.lang.ParsedText("cmd.scmd.exec.list.no", args.user["room-id"], args.user.username);
                    
                    var response = "";

                    Object.keys(cmds).forEach(async (value, index, array) => {
                        if (index == (array.length - 1)) response = response + `${args.prefix}${value}`;
                        else response = response + `${args.prefix}${value}, `;
                    });

                    return await args.lang.ParsedText("cmd.scmd.exec.list.response", args.user["room-id"], args.user.username, args.channel, response);
                case "mk":
                    var command = args.msg_args[2];
                    var response = args.msg_args;

                    delete response[0];
                    delete response[1];
                    delete response[2];

                    var reply = await args.staticcmd.makeStaticCommand(command, response.join(' '), args.user["room-id"]);

                    if (reply) return await args.lang.ParsedText("cmd.scmd.exec.make.success", args.channel, args.user.username, `${args.prefix}${command}`);
                    else return await args.lang.ParsedText("cmd.scmd.exec.make.failure", args.channel, args.user.username, `${args.prefix}${command}`);
                case "rm":
                    var command = args.msg_args[2];

                    var reply = await args.staticcmd.removeStaticCommand(command, args.user["room-id"]);

                    if (reply) return await args.lang.ParsedText("cmd.scmd.exec.rm.success", args.channel, args.user.username, `${args.prefix}${command}`);
                    else return await args.lang.ParsedText("cmd.scmd.exec.rm.failure", args.channel, args.user.username, `${args.prefix}${command}`);
                case "ch":
                    var command = args.msg_args[2];
                    var response = args.msg_args;

                    delete response[0];
                    delete response[1];
                    delete response[2];

                    var reply = await args.staticcmd.changeStaticCommand(command, response.join(' '), args.user["room-id"]);

                    if (reply) return await args.lang.ParsedText("cmd.scmd.exec.ch.success", args.channel, args.user.username, `${args.prefix}${command}`);
                    else return await args.lang.ParsedText("cmd.scmd.exec.ch.failure", args.channel, args.user.username, `${args.prefix}${command}`);
                default:
                    break;
            }
        }
    }
}

let inCooldown = [];