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
const express = require("express");
const { existsSync, readFileSync } = require("fs");
const { ApolloClient } = require("../apollo/ApolloClient");

const botapi = require("./routers/api");

class WebClient {
    constructor (apolloClient, port, directoryName) {
        this.port = port || 12906;
        this.directoryName = directoryName || `${__dirname}`;
        this.apolloClient = apolloClient.getClient();

        this.app = express();
    }

    async create() {
        this.app.get("/status", async (req, res) => {
            function formatTime(seconds) {
                function pad(s) {
                    return (s < 10 ? '0': '') + s;
                }
    
                var days = Math.floor(seconds / (60 * 60 * 24));
                var hours = Math.floor(seconds / (60 * 60) % 24);
                var minutes = Math.floor(seconds % (60 * 60) % 24);
                var sec = Math.floor(seconds % 60);
    
                return `${days} d. ${pad(hours)}:${pad(minutes)}:${pad(sec)}`;
            }
            var stats = JSON.parse(readFileSync("storage/stats.json", {encoding: "utf-8"}));
            var json = {
                bot: {
                    uptime: formatTime(process.uptime()),
                    latency: await this.apolloClient.ping(),
                    landedin: await this.apolloClient.getChannels()
                },
                stats: {
                    trackedchatlinesduringbotworking: stats.amountofchatlinesweretracked,
                    executedcommandsalltime: stats.allexecutedcommands
                },
                roles: await JSON.parse(readFileSync("storage/roles.json", {encoding: "utf-8"})),
                stvemotes: await JSON.parse(readFileSync("storage/emotes.json", {encoding: "utf-8"}))
            }
            res.json(json).status(200);
        });
        this.app.listen(this.port, async () => console.log(`* API Server is running on port ${this.port}...`));
    }

    async dispose() {
    }
}

module.exports = {WebClient};