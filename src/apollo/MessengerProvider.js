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