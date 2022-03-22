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

const {
    existsSync,
    writeFileSync,
    readFileSync
} = require("fs");
const tmi = require("tmi.js");
const twitchreq = require("twitchrequest");
const SevenTVEmoteUpdater = require("../utils/SevenTVEmoteUpdater");

class AnonymousTTV {
    constructor(options = {
        channels: any,
        online_client: any,
        ping_reply: {
            match: string,
            messenger_client: any
        },
        telegram_supa_id: any
    }) {
        this.options = options;
        this.match = new RegExp(options.ping_reply.match);
        this.online_client = this.options.online_client;
        this.messenger_client = this.options.ping_reply.messenger_client;
        this.telegram_supa_id = this.options.telegram_supa_id;

        this.client = new tmi.Client({
            connection: {
                reconnect: true,
                secure: true
            },
            channels: this.options.channels
        });
    }

    async enableAnonymous() {
        this.client.connect();

        this.client.on("message", (target, user, msg, self) => {
            const date = new Date();

            function getStringOfMonth() {
                switch (date.getUTCMonth() + 1) {
                    case 1:return "January"
                    case 2:return "February"
                    case 3:return "March"
                    case 4:return "April"
                    case 5:return "May"
                    case 6:return "June"
                    case 7:return "July"
                    case 8:return "August"
                    case 9:return "September"
                    case 10:return "October"
                    case 11:return "November"
                    case 12:return "December"
                    default:return "January"
                }
            }

            if (msg.toLowerCase().includes("ilotte") || msg.toLowerCase().includes("лот")) {
                this.messenger_client.sendMessage(`🔔 ${getStringOfMonth()} ${(date.getUTCDate().toString().length == 1) ? `0${date.getUTCDate()}` : `${date.getUTCDate()}`}, ${date.getUTCFullYear()} ${target} (${user["room-id"]}) ${user["display-name"]} (${user["username"]}-${user["room-id"]}): ${msg.trim()}`, this.telegram_supa_id);
            }

            //require("../utils/Filesystem").saveMsg(target, user, msg);
            //require("../utils/Filesystem").saveUser(null, target, user, msg);
        });

        /*
        this.client.on("ban", (target, user, reason) => {
            if (reason == null) require("../utils/Filesystem").saveMsg(target, user, null, "ban", null);
        });

        this.client.on("timeout", (target, user, reason, time) => {
            if (reason == null) require("../utils/Filesystem").saveMsg(target, user, null, "timeout", {duration: time});
        });*/
    }

    async enableResponse(username, password, channels) {
        this.response_client = new tmi.Client({
            connection: {
                reconnect: true,
                secure: true
            },
            identity: {
                username: username,
                password: password
            },
            channels: channels
        });
        

        this.response_client.connect();

        this.response_client.on("connecting", (address, port) => console.log(`* Client (Person): Connecting to ${address}:${port}...`));
        this.response_client.on("connected", (address, port) => console.log(`* Client (Person): Connected to ${address}:${port}!`));

        this.response_client.on("reconnect", () => console.log(`* Client (Person): Reconnecting...`));
        this.response_client.on("disconnected", (reason) => console.log(`* Client (Person): Disconnected: ${reason}!`));
        

        this.response_client.on("message", (target, user, msg, self) => {
            if (self) return;

            // "pajaS 🚨 ALERT ":
            if (target == "#pajlada" && user["user-id"] == "82008718" && msg.trim() == "pajaS 🚨 ALERT ") {
                this.response_client.say("#pajlada", "/me dankS 🚨 ALERT! ");
                this.response_client.say("#pwgood", "/me PepeS 🚨 ПИЗДЕЦ! ");
            }

            // Supibot pinged me:
            if (msg.includes("ilotterytea") && user["user-id"] == "68136884") {
                this.response_client.say(target, `/me FeelsOkayMan 👉 🚪 🔥 `);
            }
        });
    }

    async enableRequests(client_id, client_secret, channels, alert_msgs, alert_subs) {
        this.request_client = new twitchreq.Client({
            channels: channels,
            client_id: client_id,
            client_secret: client_secret,
            interval: 3
        });

        this.request_client.on("ready", () => console.log("* Requests are ready!"));

        this.request_client.on("live", (stream) => {
            this.online_client.say("#ilotterytea", `${alert_msgs[0].replace("{name}", stream.title).replace("{category}", stream.game)}${alert_subs}`);
        });

        this.request_client.on("unlive", (stream) => {
            this.online_client.say("#ilotterytea", `${alert_msgs[1]}`);
        });

        this.request_client.on("follow", (user, stream) => {
            this.online_client.say("#ilotterytea", `@${user.name}, thanks for following! xqcL`);
        });

    }
}
class ClientTTV {
    constructor(options = {
        username: string,
        password: string,
        channels: string,
        prefix: string,
        users: Array
    }) {
        this.channels = options.channels;

        this.client = new tmi.Client({
            connection: {
                reconnect: true,
                secure: true
            },
            channels: this.channels,
            identity: {
                username: options.username,
                password: options.password
            }
        });
        this.prefix = options.prefix;
        this.users = options.users;
        this.STV = new SevenTVEmoteUpdater.EmoteUpdater("ilotterytea", "7tv");
        this.emotes = {};
    }

    async enable() {
        this.STV.updateEmotes();

        this.client.connect();

        this.client.on("connecting", (address, port) => console.log(`* Client: Connecting to ${address}:${port}...`));
        this.client.on("connected", (address, port) => {
            console.log(`* Client: Connected to ${address}:${port}!`);
            this.client.say("#ilotterytea", "iLotteryteaLive ");

            if (this.STV.getNewEmotes != "") {
                this.client.say("#ilotterytea", `Added 7TV Emotes: ${this.STV.getNewEmotes}`);
            }
            if (this.STV.getDeletedEmotes != "") {
                this.client.say("#ilotterytea", `Deleted/renamed 7tv emotes: ${this.STV.getDeletedEmotes}`);
            }
        });

        this.client.on("reconnect", () => console.log(`* Client: Reconnecting...`));
        this.client.on("disconnected", (reason) => console.log(`* Client: Disconnected: ${reason}!`));

        this.client.on("message", (target, user, msg, self) => {
            if (self) return;

            this.emotes = this.STV.getEmotes;
            const args = msg.trim().split(' ');

            for (let i = 0; i < Object.keys(this.emotes).length; i++) {
                if (msg.includes(Object.keys(this.emotes)[i])) {
                    this.emotes[Object.keys(this.emotes)[i]] = this.emotes[Object.keys(this.emotes)[i]] += 1
                }
            }
            // Supibot:
            if (user["user-id"] == "68136884") {
                if (msg.includes("is now sleeping:") || msg.includes("is now AFK:")) {
                    const args = msg.trim().split(' ');

                    this.client.say(target, `NOOOO ${args[0]}`);
                }
                return;
            }

            if (args[0] == (`${this.prefix}help`)) {
                const help = existsSync(`./src/apollo/commands/${args[1].toLowerCase()}.js`) ? require(`./commands/${args[1].toLowerCase()}.js`).help : null;

                if (help == null) {
                    this.client.say(target, `@${user.username}, i du not know anythink about the ${this.prefix}${args[1].toLowerCase()} comand ❓❓ Okayeg ❓❓❓ `);
                    return;
                }

                this.client.say(target, `${help.name} ${help.description} ${(help.superUserOnly) ? "Only for Supa Users!" : ""}`);

                return;
            }

            if (msg.startsWith(this.prefix)) {
                if (existsSync(`./src/apollo/commands/${args[0].split(this.prefix)[1].toLowerCase()}.js`)) {
                    require(`./commands/${args[0].split(this.prefix)[1].toLowerCase()}.js`).run(this.client, target, user, msg, {
                        emote_data: this.emotes,
                        emote_updater: this.STV,
                        join: JSON.parse(readFileSync(`./options.json`, {encoding: "utf-8"}))["join"]
                    });
                }
                return;
            }

        });

        setInterval(() => {
            writeFileSync(`./saved/emote_data.json`, JSON.stringify(this.emotes, null, 2), {
                encoding: "utf-8"
            });
            console.log("* Emote file saved!");
        }, 90000);
        
        process.on("SIGTERM", (listener) => {
            writeFileSync(`./saved/emote_data.json`, JSON.stringify(this.emotes, null, 2), {
                encoding: "utf-8"
            });
            console.log("* Emote file saved!");
        });

    }

}

module.exports = {
    AnonymousTTV,
    ClientTTV
};