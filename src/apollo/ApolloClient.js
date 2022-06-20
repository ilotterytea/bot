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
const { existsSync, readFileSync, writeFileSync } = require("fs");
const tmi = require("tmi.js");
const logger = require("./utils/ApolloLogger");
const { TwitchGQL } = require("./utils/HelixTwitch");
const { SevenTVEmoteUpdater } = require("./utils/SevenTVEmoteUpdater");
const { TranslationManager } = require("./utils/TranslationManager");

class ApolloClient {
    constructor (options = {username: "", password: "", channelsToJoin: [], prefix: ""}, storage, twitchgql) {
        this.storage        = storage;
        this.options        = options;
        this.gql            = twitchgql;
        this.client         = new tmi.Client({
            connection: {reconnect: true, secure: true},
            identity: {username: this.options.username,password: this.options.password},
            channels: this.options.channelsToJoin,
            options: {debug: true}
        });
        this.EmoteUpdater   = new SevenTVEmoteUpdater(this.options.channelsToJoin);
        this.Translations   = new TranslationManager(this.storage, "data/langs");
    }

    async create() {
        // Load translation keys:
        await this.Translations.LoadLanguageFiles();
        // Load and update 7TV emotes:
        await this.EmoteUpdater.LoadEmotes(this.storage.emotes);
        await this.EmoteUpdater.UpdateEmotes();

        // Connect to the Twitch potato servers:
        this.client.connect();

        // Message event:
        this.client.on("message", async (target, user, msg, self) => {
            try {
                // Don't run further code on bot message:
                if (self) return;

                this.EmoteUpdater.UpdateEmoteCounter(target, msg);

                // Arguments:
                var args = {
                    target:     target,
                    channel:    target.slice(1, target.length),
                    user:       user,
                    msg:        msg,
                    msg_args:   msg.split(' '),
                    gql:        this.gql,
                    storage:    this.storage,
                    lang:       this.Translations,
                    apollo:     this,
                    prefix:     this.storage.prefix,
                    emotes:     await this.EmoteUpdater.getEmotes,
                    role:    (this.storage.roles.authority.includes(user["user-id"])) ? "su" : (user.username == target.slice(1, target.length)) ? "br" : (this.storage.roles.feelingspecial.includes(user["user-id"])) ? "dank" : null
                }
                
                if (!(args.channel in this.storage.stats.chat_lines)) {
                    this.storage.stats.chat_lines[args.channel] = 0;
                    this.storage.stats.chat_lines[args.channel] = this.storage.stats.chat_lines[args.channel] += 1;
                } else {
                    this.storage.stats.chat_lines[args.channel] = this.storage.stats.chat_lines[args.channel] += 1;
                }

                // lol
                if (msg == "NUTS" && target == "#fedotir") {
                    if (!(args.channel in this.storage.stats.tests)) {
                        this.storage.stats.tests[args.channel] = 0;
                    }
                    this.storage.stats.tests[args.channel] = this.storage.stats.tests[args.channel] += 1;
                    return;
                }
                if (msg == "test" && target != "#fedotir") {
                    if (!(args.channel in this.storage.stats.tests)) {
                        this.storage.stats.tests[args.channel] = 0;
                    }
                    this.storage.stats.tests[args.channel] = this.storage.stats.tests[args.channel] += 1;
                    this.client.say(target, await this.Translations.ParsedText("test.test", args.channel, "test", this.storage.stats.tests[args.channel]));
                    return;
                }

                if (args.storage.roles.permabanned.includes(user["user-id"])) return;

                // Start the command processor if message starts with a prefix:
                if (msg.startsWith(this.storage.prefix)) {
                    if (!(args.channel in this.storage.stats.executed_commands)) {
                        this.storage.stats.executed_commands[args.channel] = 0;
                    }

                    if (args.msg_args[0] == "!dispose" && args.role == "su" && (target == "#ilotterytea" || target == "#fembajtea")) {
                        this.storage.stats.executed_commands[args.channel] = this.storage.stats.executed_commands[args.channel] += 1;
                        var a_emotes = ["docLeave", "peepoLeave", "ppPoof", "monkaGIGAftSaj"];
                        this.client.say(target, await this.Translations.ParsedText("leave", args.channel, a_emotes[Math.floor(Math.random() * (a_emotes.length - 1))]));
                        this.dispose();
                        return;
                    }

                    // Get some info about command with the !help command:
                    if (args.msg_args[0] == `${this.storage.prefix}help`) {
                        if (args.msg_args.length == 1) {
                            this.storage.stats.executed_commands[args.channel] = this.storage.stats.executed_commands[args.channel] += 1;
                            this.client.say(target, await this.Translations.ParsedText("cmd.help.exec.help", args.channel, args.user.username));
                            return;
                        }
                        // "!help help":
                        if (args.msg_args[1] == "help") {
                            this.storage.stats.executed_commands[args.channel] = this.storage.stats.executed_commands[args.channel] += 1;
                            this.client.say(target, await this.Translations.ParsedText("cmd.help.exec.lolresponse", args.channel, args.user.username));
                            return;
                        }
                        // Say info about command:
                        if (existsSync(`src/apollo/commands/${args.msg_args[1]}.js`)) {
                            this.storage.stats.executed_commands[args.channel] = this.storage.stats.executed_commands[args.channel] += 1;
                            const cmd = `cmd.${args.msg_args[1]}`
                            this.client.say(target, await this.Translations.ParsedText("cmd.help.exec.response", args.channel, await this.Translations.PlainText(`${cmd}.name`), await this.Translations.PlainText(`${cmd}.desc`)));
                        }
                        return;
                    }

                    // Execute the command if it exists:
                    if (existsSync(`src/apollo/commands/${args.msg_args[0].slice(1, args.msg_args[0].length)}.js`)) {
                        const cmd = require(`./commands/${args.msg_args[0].slice(1, args.msg_args[0].length)}.js`);
                        const cmdperm = cmd.permissions.join('');
                        let runcmd = false;

                        // Compare the user's role and command permission:
                        switch (true) {
                            case (cmd.permissions.includes("pub")):
                                runcmd = true;
                                break;
                            case (cmdperm == "subrdank" && (args.role == "su" || args.role == "br" || args.role == "dank")):
                                runcmd = true;
                                break;
                            case (cmdperm == "su" && args.role == "su"):
                                runcmd = true;
                                break;
                            case (cmdperm == "subr" && (args.role == "su" || args.role == "br")):
                                runcmd = true;
                                break;
                            case (cmdperm == "sudank" && (args.role == "su" || args.role == "dank")):
                                runcmd = true;
                                break;
                            case (cmdperm == "br" && args.role == "br"):
                                runcmd = true;
                                break;
                            case (cmdperm == "brdank" && (args.role == "br" || args.role == "dank")):
                                runcmd = true;
                                break;
                            case (cmdperm == "dank" && args.role == "dank"):
                                runcmd = true;
                                break;
                            default:
                                break;
                        }
                        if (runcmd) {
                            try {
                                this.storage.stats.executed_commands[args.channel] = this.storage.stats.executed_commands[args.channel] += 1;
                                var response = await cmd.execute(args);
                                if (response != null) {
                                    this.client.say(target, response);
                                }
                            } catch (err) {
                                this.client.say(target, await this.Translations.ParsedText("error", args.channel, args.user.username));
                                logger(`Error occurred during the execution of ${args.msg_args[0]} command: ${err}`, "err", true);
                            }
                        }
                    }
                }
            } catch(err) {
                logger(err, "err", true);
                this.dispose();
            }
        });

        setInterval(async () => {
            await this.EmoteUpdater.UpdateEmotes();
            setTimeout(async () => {
                var delemotes = this.EmoteUpdater.getDeletedEmotes;
                var newemotes = this.EmoteUpdater.getNewEmotes;

                this.options.channelsToJoin.forEach(async (value, index, array) => {
                    var target = value.slice(1, value.length);

                if (newemotes[target] != '' && newemotes[target] != undefined) this.client.action(value, await this.Translations.ParsedText("emoteupdater.new_emotes", value.slice(1, value.length), "[STV]", newemotes[target]));
                if (delemotes[target] != '' && delemotes[target] != undefined) this.client.action(value, await this.Translations.ParsedText("emoteupdater.deleted_emotes", value.slice(1, value.length), "[STV]", delemotes[target]));
                });
                await this.SaveStorage();
            }, 2500);
        }, 30000);

        // Internet connection events:
        this.client.on("connecting", async (address, port) => logger(`Client is connecting to ${address}:${port}...`, "log", true));
        this.client.on("connected", async (address, port) => {
            logger(`Client is connected to ${address}:${port}!`, "log", true);

            var delemotes = this.EmoteUpdater.getDeletedEmotes;
            var newemotes = this.EmoteUpdater.getNewEmotes;

            this.options.channelsToJoin.forEach(async (value, index, array) => {
                if (value == "#ilotterytea") {
                    var a_emotes = ["ShelbyWalk", "peepoArrive", "billyArrive", "docArrive", "WalterArrive"];
                    this.client.say(value, await this.Translations.ParsedText("arrive", value.slice(1, value.length), a_emotes[Math.floor(Math.random() * (a_emotes.length - 1))]));
                }

                var target = value.slice(1, value.length);

                if (newemotes[target] != '' && newemotes[target] != undefined) this.client.action(value, await this.Translations.ParsedText("emoteupdater.new_emotes", value.slice(1, value.length), "[7TV]", newemotes[target]));
                if (delemotes[target] != '' && delemotes[target] != undefined) this.client.action(value, await this.Translations.ParsedText("emoteupdater.deleted_emotes", value.slice(1, value.length), "[7TV]", delemotes[target]));
                });
        });
        this.client.on("reconnect", async () => logger(`Client is reconnecting...`, "log", true));
        this.client.on("disconnected", async (reason) => logger(`Client is disconnected! Reason: ${reason}`, "warn", true));
    }

    getClient() {
        return this.client;
    }

    async dispose() {
        this.client.disconnect();
        this.SaveStorage();
        logger(`Client is disposed!`, "log", true);
        process.exit(0);
    }

    async SaveStorage() {
        writeFileSync("storage/storage.json", JSON.stringify(this.storage, null, 2), {encoding: "utf-8"});
        logger("Storage saved!", "log", true);
    }
}

module.exports = {ApolloClient};