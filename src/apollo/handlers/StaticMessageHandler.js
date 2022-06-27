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

const { readFileSync, writeFileSync } = require("fs");


class StaticMessageHandler {
    /**
     * Static Message handler.
     * @param file_path the path to the file with the messages and the channels attached to them
     */
    constructor (file_path) {
        this.file_path = file_path;
        this.cmds = JSON.parse(readFileSync(this.file_path, {encoding: "utf-8"}));
    }

    async check(command, channel_id, ...args) {
        if (!(channel_id in this.cmds)) return false;
        if (!(command in this.cmds[channel_id])) return false;

        return this.cmds[channel_id][command].response.replace("%username%", args[0]);
    }

    async removeStaticCommand(command, channel_id) {
        try {
            if (!(channel_id in this.cmds)) return false;
            if (!(command in this.cmds[channel_id])) return false;

            delete this.cmds[channel_id][command];

            return true;
        } catch (err) {
            console.error(err);
        }
    }

    async changeStaticCommand(command, response, channel_id) {
        try {
            if (!(channel_id in this.cmds)) return false;
            if (!(command in this.cmds[channel_id])) return false;
            console.log(this.cmds[channel_id][command])

            this.cmds[channel_id][command].response = response;

            return true;
        } catch (err) {
            console.error(err);
        }
    }

    async makeStaticCommand(command, response, channel_id) {
        try {
            if (!(channel_id in this.cmds)) this.cmds[channel_id] = {};
            if (command in this.cmds[channel_id]) return false;

            this.cmds[channel_id][command] = {};
            this.cmds[channel_id][command].response = response;

            return true;
        } catch (err) {
            console.error(err);
        }
    }

    getStaticCommand(command, channel_id) {
        if (!(channel_id in this.cmds)) return false;
        if (!(command in this.cmds[channel_id])) return false;

        return this.cmds[channel_id][command];
    }

    getAllStaticCommands(channel_id) {
        if (!(channel_id in this.cmds)) return false;

        return this.cmds[channel_id];
    }

    async save() {
        writeFileSync(this.file_path, JSON.stringify(this.cmds, null, 2), {encoding: "utf-8"});
    }
}

module.exports = StaticMessageHandler;