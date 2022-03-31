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

const { existsSync, writeFileSync, readFileSync } = require("fs");
const SevenTV = require("7tv");
const { default: axios } = require("axios");

/**
 * Load and check the 7tv emotes.
 * @param {*} username Username.
 */
class EmoteUpdater {
    constructor (usernames) {
        this.api = SevenTV();
        this.usernames = usernames;
        this.emotes = {};
        this.deleted_emotes = {};
        this.new_emotes = {};
        this.first_startup = false;
    }

    get getDeletedEmotes() {
        return this.deleted_emotes;
    }

    get getNewEmotes() {
        return this.new_emotes;
    }

    get getEmotes() {
        return this.emotes;
    }

    async updateEmotes(emote_dictionary) {
        this.emotes = emote_dictionary;

        var count = 0;

        // Reset the values
        this.new_emotes = {};
        this.deleted_emotes = {};

        this.usernames.forEach(async (value, index, array) => {
            const api_emotes = await this.api.fetchUserEmotes(value); // Fetch user's 7tv channel emotes.
            //const user_id = await (await axios.get(`https://decapi.me/twitch/id/${value}`)).data;
            //const bttv_emotes = await (await axios.get(`https://api.betterttv.net/3/cached/users/twitch/${user_id}`)).data;
            //const ffz_emotes = await (await axios.get(`https://api.frankerfacez.com/v1/room/${value}`)).data;

            let formatted_emotes = {};
            this.deleted_emotes[value] = "";
            this.new_emotes[value] = "";

            formatted_emotes["stv"] = {};
            formatted_emotes["bttv"] = {};
            formatted_emotes["ffz"] = {};
            formatted_emotes["ttv"] = {};

            // Add the api_emotes names to the array:
            for (let i = 0; i < api_emotes.length; i++) {
                formatted_emotes["stv"][api_emotes[i].name] = 0;
            }


            // Create a dictionary for new users:
            if (eval(`!("${value}" in this.emotes)`)) {
                this.emotes[value] = {}
                this.emotes[value]["stv"] = {}
                this.emotes[value]["bttv"] = {}
                this.emotes[value]["ffz"] = {}
                this.emotes[value]["ttv"] = {}
            }

            // Check for new emotes:
            for (let i = 0; i < api_emotes.length; i++) {
                if (eval(`!("${api_emotes[i].name}" in this.emotes["${value}"].stv)`)) {
                    this.emotes[value].stv[api_emotes[i].name] = 0;
                    this.new_emotes[value] = this.new_emotes[value] += `${api_emotes[i].name} `;
                }
            }

            // Check the deleted emotes:
            for (let i = 0; i < Object.keys(this.emotes[value]["stv"]).length; i++) {
                if (eval(`!("${Object.keys(this.emotes[value]["stv"])[i]}" in formatted_emotes["stv"])`)) {
                    this.deleted_emotes[value] = this.deleted_emotes[value] += `${Object.keys(this.emotes[value]["stv"])[i]} `;
                }
            }
            
            // Remove deleted emotes from this.emotes:
            for (let i = 0; i < this.deleted_emotes[value].split(' ').length; i++) {
                delete this.emotes[value]["stv"][this.deleted_emotes[value].split(' ')[i]];
            }

            count++;
            if (count == array.length) {
                writeFileSync("./saved/emotes.json", JSON.stringify(this.emotes, null, 2), {encoding: "utf-8"});
                console.log(`* 7TV channel emotes were updated!`);
            }
        });
    }
}

module.exports = {EmoteUpdater};