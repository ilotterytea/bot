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
                this.response_client.action("#pajlada", "dankS 🚨 ALERT! ");
                this.response_client.action("#pwgood", "xqcA 🚨 ИНОПЛАНЕТЯНЕ НАСТУПАЮТ! ");
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
    constructor(options) {
        this.options = options;

        this.client = new tmi.Client({
            connection: {
                reconnect: true,
                secure: true
            },
            channels: this.options.join.as_client,
            identity: {
                username: this.options.credentials.twitch.username[0],
                password: this.options.credentials.twitch.password[0]
            }
        });
        this.prefix = this.options.prefix;
        this.users = this.options.users;
        this.STV = new SevenTVEmoteUpdater.EmoteUpdater("ilotterytea", "7tv");
        this.emotes = {};
        this.firstTimeConnected = false;
    }

    async enable() {
        this.client.connect();
        this.STV.updateEmotes();

        this.client.on("connecting", (address, port) => console.log(`* Client: Connecting to ${address}:${port}...`));
        this.client.on("connected", (address, port) => {
            console.log(`* Client: Connected to ${address}:${port}!`);
            if (!this.firstTimeConnected) {
                this.client.say("#ilotterytea", "iLotteryteaLive ");
                this.firstTimeConnected = true;
            };

            if (this.STV.getNewEmotes != "") {
                this.client.say("#ilotterytea", `Added 7TV channel emotes: ${this.STV.getNewEmotes}`);
            }
            if (this.STV.getDeletedEmotes != "") {
                this.client.say("#ilotterytea", `Deleted/renamed 7TV channel emotes: ${this.STV.getDeletedEmotes}`);
            }
        });

        this.client.on("reconnect", () => console.log(`* Client: Reconnecting...`));
        this.client.on("disconnected", (reason) => console.log(`* Client: Disconnected: ${reason}!`));

        this.client.on("message", (target, user, msg, self) => {
            if (self) return;

            this.emotes = this.STV.getEmotes;

            const cmd_args = {
                emote_data: this.emotes,
                join: JSON.parse(readFileSync(`./options.json`, {encoding: "utf-8"}))["join"],
                images: this.images
            };

            const args = msg.trim().split(' ');

            for (let i = 0; i < Object.keys(this.emotes).length; i++) {
                if (args.includes(Object.keys(this.emotes)[i])) {
                    this.emotes[Object.keys(this.emotes)[i]] = this.emotes[Object.keys(this.emotes)[i]] += 1
                }
            }

            // Respond to Supibot messages (when someone uses $afk):
            if (user["user-id"] == "68136884") {
                if (msg.includes("is now sleeping:") || msg.includes("is now AFK:")) {
                    const args = msg.trim().split(' ');

                    this.client.say(target, `NOOOO ${args[0]}`);
                }
                return;
            }


            if (msg.startsWith(this.prefix)) {
                const cmd = args[0].split(this.prefix)[1].toLowerCase();

                if (cmd == "help") {
                    if (args.length == 1) {
                        this.client.say(target, `@${user.username}, List of my available commands: https://github.com/NotDankEnough/notdankenough/blob/master/md/iLotteryteaLive.md#twitch-commands FeelsOkayMan `);
                        return;
                    }

                    const help = existsSync(`./src/apollo/commands/${args[1].toLowerCase()}.js`) ? require(`./commands/${args[1].toLowerCase()}.js`).help : null;
    
                    if (help == null) {
                        this.client.say(target, `@${user.username}, i du not know anythink about the ${this.prefix}${args[1].toLowerCase()} comand ❓❓ Okayeg ❓❓❓ `);
                        return;
                    }
    
                    this.client.say(target, `FeelsDankMan 📖 ${help.name} ${help.description} Cooldown: ${help.cooldownMs / 1000} sec. ${(help.superUserOnly) ? "Only for Supa Users!" : ""}`);
                    return;
                }

                if (existsSync(`./src/apollo/commands/${cmd}.js`)) {
                    const help = require(`./commands/${cmd}.js`).help;

                    // If the command is for super-users, check if the sender is a super-user:
                    if (help.superUserOnly) {
                        if (this.users.supa_user_ids.includes(user["user-id"])) {
                            require(`./commands/${cmd}.js`).run(this.client, target, user, msg, cmd_args);
                        } else {
                            this.client.say(target, `@${user.username}, u du not hav permision tu du that! Sadeg `);
                        }
                        return;
                    }

                    require(`./commands/${cmd}.js`).run(this.client, target, user, msg, cmd_args);
                }
                return;
            }
        });

        // Subscriptions, cheer events:
        this.client.on("cheer", async (target, user, msg) => (target == "#ilotterytea") ? this.client.say(target, `heCrazy @${user.username} just cheered Bits ${user.bits} `) : "");
        this.client.on("subscription", async (target, username, methods, msg, user) => (target == "#ilotterytea") ? this.client.say(target, `PepeHands @${username} accidentally subscribed to iLotterytea!`) : "");
        this.client.on("resub", async (target, username, streakMonths, msg, user, methods) => (target == "#ilotterytea") ? this.client.say(target, `heCrazy @${username} has been subscribed to iLotterytea for ${user["msg-param-cumulative-months"]} months!`) : "");
        this.client.on("subgift", async (target, username, streakMonths, recipient, methods, user) => (target == "#ilotterytea") ? this.client.say(target, `heCrazy heCrazy heCrazy @${username} has gifted a subscription to @${recipient}! heCrazy heCrazy heCrazy `) : "");
        this.client.on("raided", (target, username, viewers) => (target == "#ilotterytea") ? this.client.say(target, `doctorDance TombRaid @${username} has raided the channel with ${(viewers == 1) ? `${viewers} viewer` : `${viewers} viewers`}`) : "");

        // Ban, clearchat, timeout events:
        this.client.on("clearchat", async (target) => (target == "#ilotterytea") ? this.client.say(target, "NothingHappened ") : "");
        this.client.on("ban", async (target, username, reason) => (target == "#ilotterytea") ? this.client.say(target, `monkaLaugh 👍 I LOVE ${target.slice(1, target.length).toUpperCase()}! `) : "");

        // Notice:
        this.client.on("notice", async (target, msgid, msg) => {
            switch (msgid) {
                case "msg_banned":
                    this.options.suspended.push(target.slice(1, target.length));
                    this.options.join.as_client = this.options.join.as_client.filter(u => u !== target.slice(1, target.length));
                    writeFileSync("./options.json", JSON.stringify(this.options, null, 2), {encoding: "utf-8"});
                    break;
                case "msg_channel_suspended":
                    this.options.suspended.push(target.slice(1, target.length));
                    this.options.join.as_client = this.options.join.as_client.filter(u => u !== target.slice(1, target.length));
                    writeFileSync("./options.json", JSON.stringify(this.options, null, 2), {encoding: "utf-8"});
                    break;
                case "tos_ban":
                    this.options.suspended.push(target.slice(1, target.length));
                    this.options.join.as_client = this.options.join.as_client.filter(u => u !== target.slice(1, target.length));
                    writeFileSync("./options.json", JSON.stringify(this.options, null, 2), {encoding: "utf-8"});
                    break;
                default:
                    console.log(`${target} - ${msgid} - ${msg}`);
                    break;
            }
        });

        setInterval(() => {
            writeFileSync(`./saved/emote_data.json`, JSON.stringify(this.emotes, null, 2), {
                encoding: "utf-8"
            });
            console.log("* Emote file saved!");
        }, 90000);

        // Reconnects to Twitch servers to update 7TV channel emotes (I couldn't do it any other way):
        setInterval(() => {
            this.STV.updateEmotes();
            this.client.disconnect();
            setTimeout(() => {
                this.client.connect();
            }, 1000);
            console.log("* 7TV channel emotes has been updated!");
        }, 43200000);
        
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