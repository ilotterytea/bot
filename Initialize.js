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
async function run() {
    // Check for the required system folders (/saved/):
    check();

    // Enable the Telegram bot client:
    const msgClient = new MessengerProvider(process.env.ms_credentials_password);
    msgClient.enable();

    // Bot Twitch client:
    const client = new ClientTTV({
        username: process.env.tv_credentials_username.split(',')[0],
        password: process.env.tv_credentials_password.split(',')[0],
        channels: process.env.tv_options_joinasclient.split(',')
    });
    client.enable();

    // Anonymous client:
    const anonymous = new AnonymousTTV({
        channels: process.env.tv_options_joinasanonymous.split(','),
        online_client: client,
        ping_reply: {
            match: process.env.tv_options_pinghim,
            messenger_client: msgClient
        }
    });

    anonymous.enableAnonymous();

    anonymous.enableRequests({
        client_id: process.env.tv_credentials_clientid,
        client_secret: process.env.tv_credentials_clientsec,
        channels: process.env.tv_options_joinasperson.split(',').push("ilotterytea"),
        alerts: {
            msg: process.env.tv_options_alerts_msg.split(','),
            subs: process.env.tv_options_alerts_sub
        }
    });

    /*
    anonymous.enableResponse({
        username: process.env.tv_credentials_username.split(',')[1],
        password: process.env.tv_credentials_password.split(',')[1],
        channels: ["ilotterytea"]
    });*/

}

run();