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

const { existsSync } = require("fs");
const TelegramBot = require("node-telegram-bot-api");

class MessengerProvider {
    constructor (oauth_token) {
        this.client = new TelegramBot(oauth_token);
    }

    async enable() {
        this.client.on("message", (msg, data) => {
            if (msg.startsWith("/")) {
                if (existsSync(`./src/apollo/commands/${msg.split(' ')[0].split('/')[1]}.js`)) {
                    require(`./commands/${msg.split(' ')[0].split('/')[1]}.js`).msRun(this.client, msg, data);
                };
            }
        });
    }

    sendMessage(content, recipient_id) {
        this.client.sendMessage(recipient_id, content);
    }
}

module.exports = {MessengerProvider}