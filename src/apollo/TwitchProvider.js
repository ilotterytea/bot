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
        function usernameFix(userlist) {
            let usernames = [];
            userlist.forEach((value, index, array) => {
                usernames.push(value.slice(1, value.length));
            });
            return usernames;
        
        }
        this.STV = new SevenTVEmoteUpdater.EmoteUpdater(usernameFix(this.options.join.as_client));
    }


    async enable() {
        await this.STV.updateEmotes(JSON.parse(readFileSync("./saved/emotes.json", {encoding: "utf-8"})));
        this.client.connect();

        this.client.on("connecting", (address, port) => console.log(`* Client: Connecting to ${address}:${port}...`));
        this.client.on("connected", (address, port) => {
            console.log(`* Client: Connected to ${address}:${port}!`);

            this.options.join.as_client.forEach((value, index, array) => {
                if (value == "#ilotterytea") this.client.say(value, "iLotteryteaLive ");

                var new_emotes = this.STV.getNewEmotes;
                var del_emotes = this.STV.getDeletedEmotes;

                if (new_emotes[value.slice(1, value.length)] != "") {
                    this.client.say(value, `Added 7TV channel emotes: ${new_emotes[value.slice(1, value.length)]}`);
                }
                if (del_emotes[value.slice(1, value.length)] != "") {
                    this.client.say(value, `Deleted/renamed 7TV channel emotes: ${del_emotes[value.slice(1, value.length)]}`);
                }
            });
        });

        this.client.on("reconnect", () => console.log(`* Client: Reconnecting...`));
        this.client.on("disconnected", (reason) => console.log(`* Client: Disconnected: ${reason}!`));

        this.client.on("message", (target, user, msg, self) => {
            this.emotes = this.STV.getEmotes;
            if (self) return;

            const cmd_args = {
                emote_data: this.emotes,
                join: JSON.parse(readFileSync(`./options.json`, {encoding: "utf-8"}))["join"]
            };

            const args = msg.trim().split(' ');

            for (let i = 0; i < args.length; i++) {
                for (let j = 0; j < Object.keys(eval(`this.emotes["${target.slice(1, target.length)}"]["stv"]`)).length; j++) {
                    if (args[i] == Object.keys(eval(`this.emotes["${target.slice(1, target.length)}"]["stv"]`))[j]) {
                        eval(`this.emotes["${target.slice(1, target.length)}"]["stv"][Object.keys(this.emotes["${target.slice(1, target.length)}"]["stv"])[j]] = this.emotes["${target.slice(1, target.length)}"]["stv"][Object.keys(this.emotes["${target.slice(1, target.length)}"]["stv"])[j]] += 1`);
                    }
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
                        this.client.say(target, `@${user.username}, List of my available commands: https://github.com/NotDankEnough/notdankenough/blob/master/md/iLotteryteaLive.md#twitch-commands FeelsDankMan 🔎 📖 `);
                        return;
                    }

                    const help = existsSync(`./src/apollo/commands/${args[1].toLowerCase()}.js`) ? require(`./commands/${args[1].toLowerCase()}.js`).help : null;
    
                    if (help == null) {
                        this.client.say(target, `@${user.username}, i du not know anythink about the ${this.prefix}${args[1].toLowerCase()} comand ❓❓ Okayeg ❓❓❓ `);
                        return;
                    }
    
                    this.client.say(target, `FeelsDankMan 📖 ${help.name} ${help.description} Cooldown: ${help.cooldownMs / 1000} sec. ${(help.superUserOnly) ? "Only for Supa Users!" : ""}${(help.authorOnly) ? "Only for bot creator!" : ""}`);
                    return;
                }

                if (existsSync(`./src/apollo/commands/${cmd}.js`)) {
                    const help = require(`./commands/${cmd}.js`).help;

                    // If the command is for super-users, check if the sender is a super-user:
                    if (help.superUserOnly) {
                        if (this.users.supa_user_ids[target.slice(1, target.length)].includes(user["user-id"])) {
                            require(`./commands/${cmd}.js`).run(this.client, target, user, msg, cmd_args);
                        } else {
                            this.client.say(target, `@${user.username}, u du not hav permision tu du that! Sadeg `);
                        }
                        return;
                    }

                    if (help.authorOnly) {
                        if (user["user-id"] == "191400264") {
                            require(`./commands/${cmd}.js`).run(this.client, target, user, msg, cmd_args);
                            return;
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

        this.client.on("clearchat", async (target) => (target == "#ilotterytea") ? this.client.say(target, "NothingHappened ") : "");

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

        // Updates 7tv channel emotes every 45 minutes:
        setInterval(async () => {
            setTimeout(async () => {
                await this.STV.updateEmotes(this.emotes);
            }, 1500);
            
            setTimeout(() => {
                var new_emotes = this.STV.getNewEmotes;
                var del_emotes = this.STV.getDeletedEmotes;

                this.options.join.as_client.forEach((value, index, array) => {
                    if (new_emotes[value.slice(1, value.length)] != "") {
                        this.client.say(value, `Added 7TV channel emotes: ${new_emotes[value.slice(1, value.length)]}`);
                    }
                    if (del_emotes[value.slice(1, value.length)] != "") {
                        this.client.say(value, `Deleted/renamed 7TV channel emotes: ${del_emotes[value.slice(1, value.length)]}`);
                    }
                });
            }, 3500);
        }, 90000);
    }
}

module.exports = {
    AnonymousTTV,
    ClientTTV
};