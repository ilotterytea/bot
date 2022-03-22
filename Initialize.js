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

// Libraries:
const { readFileSync } = require("fs");
const { MessengerProvider } = require("./src/apollo/MessengerProvider");
const { AnonymousTTV, ClientTTV} = require("./src/apollo/TwitchProvider");
const { check } = require("./src/utils/Filesystem");

// Configure the enviroment variables:
require("dotenv").config({path: "./default.env"});

// Launch the bot:
class ClientInitialize {
    constructor (options) {
        this.options = JSON.parse(readFileSync("./options.json", {encoding: "utf-8"})) || options;

        this.msgClient = new MessengerProvider(this.options.credentials.telegram_oauth);
        
        this.client = new ClientTTV({
            username: this.options.credentials.twitch.username[0],
            password: this.options.credentials.twitch.password[0],
            channels: this.options.join.as_client,
            prefix: this.options.prefix,
            users: this.options.users
        });
        this.anonymous = new AnonymousTTV({
            channels: this.options.join.as_anonymous,
            online_client: this.client,
            ping_reply: {
                messenger_client: this.msgClient
            },
            telegram_supa_id: this.options.users.ms_supa_user_ids
        });
    }

    async run() {
        check();

        this.msgClient.enable();
        this.client.enable();
        this.anonymous.enableAnonymous();
        this.anonymous.enableRequests(
            this.options.credentials.twitch.client_id, 
            this.options.credentials.twitch.client_secret, 
            ["ilotterytea"], 
            this.options.alerts.msg, 
            this.options.alerts.sub
            );

        this.anonymous.enableResponse(this.options.credentials.twitch.username[1], this.options.credentials.twitch.password[1], this.options.join.as_person);
    }
}

new ClientInitialize(JSON.parse(readFileSync(`./options.json`, {encoding: "utf-8"}))).run();

module.exports = {ClientInitialize};