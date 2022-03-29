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

    async updateEmotes() {
        let file_emotes = JSON.parse(readFileSync("./saved/emotes.json", {encoding: "utf-8"}));
        var count = 0;

        // Reset the values
        this.new_emotes = {};
        this.deleted_emotes = {};
        this.emotes = {}; 

        this.usernames.forEach(async (value, index, array) => {
            const api_emotes = await this.api.fetchUserEmotes(value); // Fetch user's 7tv channel emotes.
            let formatted_emotes = {};
            this.deleted_emotes[value] = "";
            this.new_emotes[value] = "";

            // Add the api_emotes names to the array:
            for (let i = 0; i < api_emotes.length; i++) {
                formatted_emotes[api_emotes[i].name] = 0;
            }

            // Create a dictionary for new users:
            if (eval(`!("${value}" in file_emotes)`)) {
                file_emotes[value] = {}
                file_emotes[value]["stv"] = {}
            }

            // Check for new emotes:
            for (let i = 0; i < api_emotes.length; i++) {
                if (eval(`!("${api_emotes[i].name}" in file_emotes["${value}"].stv)`)) {
                    file_emotes[value].stv[api_emotes[i].name] = 0;
                    this.new_emotes[value] = this.new_emotes[value] += `${api_emotes[i].name} `;
                }
            }

            // Check the deleted emotes:
            for (let i = 0; i < Object.keys(file_emotes[value]["stv"]).length; i++) {
                if (eval(`!("${Object.keys(file_emotes[value]["stv"])[i]}" in formatted_emotes)`)) {
                    this.deleted_emotes[value] = this.deleted_emotes[value] += `${Object.keys(file_emotes[value]["stv"])[i]} `;
                }
            }
            
            // Remove deleted emotes from file_emotes:
            for (let i = 0; i < this.deleted_emotes[value].split(' ').length; i++) {
                delete file_emotes[value]["stv"][this.deleted_emotes[value].split(' ')[i]];
            }

            count++;
            if (count == array.length) {
                this.emotes = file_emotes;
                writeFileSync("./saved/emotes.json", JSON.stringify(file_emotes, null, 2), {encoding: "utf-8"});
                console.log(`* 7TV channel emotes were updated!`);
            }
        });
    }
}

module.exports = {EmoteUpdater};