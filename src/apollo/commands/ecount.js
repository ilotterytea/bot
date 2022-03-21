// Libraries.
const { readFileSync } = require("fs");
const { stringify } = require("querystring");
const tmiJs = require("tmi.js");

/**
 * Help.
 */
exports.help = {
    value: true,
    name: "Emote Count!",
    author: "ilotterytea",
    description: "Shows how much of the specified emote has been used.",
    cooldownMs: 0,
    superUserOnly: false
}

/**
 * Run the command.
 * @param {*} client Client.
 * @param {*} target Target.
 * @param {*} user User.
 * @param {*} msg Message.
 * @param {*} args Arguments.
 */
exports.run = async (client, target, user, msg, args = {
    emote_data: any,
    emote_updater: any
}) => {
    const mArgs = msg.split(' ');

    if (mArgs.length == 1) {
        client.say(target, `@${user.username}, provide an emote.`);
        return;
    }

    if (mArgs[1] in args.emote_data) {
        client.say(target, `${mArgs[1]} has been used ${args.emote_data[mArgs[1]]} times.`);
        return;
    } else {
        client.say(target, `@${user.username}, I don't know what this ${mArgs[1]} emote is. Probably this emote was added recently and you need to use ${process.env.tv_options_prefix}eupdate to update the emote database.`);
    }
};