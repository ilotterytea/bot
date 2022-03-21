const { existsSync, mkdirSync } = require("fs");

module.exports.saveUser = async (client, target, user, msg, args = {}) => {
    const twitch = new TwitchApi({
        client_id: "",
        client_secret: ""
    });

    const date = new Date();

    try {
        const stvUser = await SevenTV().fetchUser(user.username);

        const previousUserData = existsSync(`././saved/pubdata/${user.username.toLowerCase()}.json`) ? JSON.parse(readFileSync(`././saved/pubdata/${user.username.toLowerCase()}.json`, {
            encoding: "utf-8"
        })) : null

        let userData = {
            bot: {
                first_time_registered: `${(previousUserData == null) ? `${date.getUTCDate()}-${date.getUTCMonth() + 1}-${date.getUTCFullYear()} ${date.getUTCHours()}:${date.getUTCMinutes()}:${date.getUTCSeconds()}.${date.getUTCMilliseconds()}` : previousUserData.bot.first_time_registered}`,
                detected_on_channels: (previousUserData == null) ? [] : previousUserData.bot.detected_on_channels,
                alias_id: `${(previousUserData == null) ? readdirSync(`././saved/pubdata`).length -= 1 : previousUserData.bot.alias_id}`
            },
            public_data: {
                username: user.username,
                user_id: user["user-id"],
                display_name: user["display-name"],
                color: user.color,
                turbo: user.turbo,
                moderator: (previousUserData == null) ? [] : previousUserData.public_data.moderator,
                subscriber: (previousUserData == null) ? [] : previousUserData.public_data.subscriber,
                broadcaster_type: "",
                description: "",
                profile_image: "",
                offline_image: "",
                view_count: 0,
                created_at: ""
            },
            "7tv": {
                id: stvUser.id,
                role: (stvUser.role.name != '') ? stvUser.role.name : `user`,
                role_color: stvUser.role.color,
                role_pos: stvUser.role.position,
                banned: stvUser.banned
            },
            last_seen: {
                msg: `${msg}`,
                timestamp: `${date.getUTCDate()}-${date.getUTCMonth()+1}-${date.getUTCFullYear()}_${date.getUTCHours()}:${date.getUTCMinutes()}:${date.getUTCSeconds()}.${date.getUTCMilliseconds()}`,
                channel: `${target}-${user["room-id"]}`
            }
        }

        let userExample = {
            id: 0,
            twitch_id: 191400264,
            username: "ilotterytea",
            display_name: "iLotterytea",
            description: "SUCKS IN ABSOLUTELY EVERYTHING. NOT A GOD GAMER. A SUBJECT TO THE LEADER OF JUICERS.",
            badges: ["Bot_Developer", ],
            isSuspended: false,
            stored_data: {
                iLotteryteaLive: {

                },
                Engine: {

                }
            }
        }

        console.log(userData);
        // Adding the channel on which the user was detected.
        (userData.bot.detected_on_channels.includes(`${target}`)) ? null: userData.bot.detected_on_channels.push(`${target}`);
        // Is the user a moderator of this channel?
        (user.mod) ? (userData.public_data.moderator.includes(`${target}`)) ? null: userData.public_data.moderator.push(`${target}`): (userData.public_data.moderator.includes(`${target}`)) ? delete userData.public_data.moderator[userData.public_data.moderator.indexOf(`${target}`)] : null;
        // Is the user a subscriber of this channel?
        (user.subscriber) ? (userData.public_data.subscriber.includes(`${target}`)) ? null: userData.public_data.subscriber.push(`${target}`): (userData.public_data.subscriber.includes(`${target}`)) ? delete userData.public_data.subscriber[userData.public_data.subscriber.indexOf(`${target}`)] : null;

        writeFileSync(`././saved/pubdata/${user.username}.json`, JSON.stringify(userData, null, 2), {
            encoding: "utf-8"
        });
    } catch (err) {
        console.error(err);
    }
};

module.exports.saveLog = async (content) => {
    const date = new Date();
    appendFileSync(`././saved/system/SYSTEM_${date.getUTCDate()}-${date.getUTCMonth() + 1}-${date.getUTCFullYear()}.log`, `[${date.getUTCDate()}-${date.getUTCMonth() + 1}-${date.getUTCFullYear()} ${date.getUTCHours()}:${date.getUTCMinutes()}:${date.getUTCSeconds()}.${date.getUTCMilliseconds()}] SYSTEM: ${content}\n`)
};

module.exports.check = async () => {
    if (!existsSync("./saved")) {
        console.log("* Folder /saved/ not exists! Creating...");
        mkdirSync("./saved");
        mkdirSync("./saved/logs");
        mkdirSync("./saved/pubdata");
        console.log("* Folders /saved/, /saved/logs/, /saved/pubdata/ created!");
    }
    console.log("* Startup checking finished!");
};