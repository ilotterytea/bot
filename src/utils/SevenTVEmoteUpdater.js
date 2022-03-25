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
    constructor (username, emotes) {
        this.username = username;
        this.emote_mode = (emotes == undefined) ? "7tv" : emotes;
        this.emotes = undefined;
        this.deleted_emotes = "";
        this.new_emotes = "";
        this.api = SevenTV();
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
        const emotes = await this.api.fetchUserEmotes(this.username); // Fetch user 7tv emotes.
        let formatted_emotes = {};
        this.deleted_emotes = "";
        this.new_emotes = "";
        this.emotes = {};
        let old_emotes = {};

        // A file of emote data exists:
        if (existsSync("./saved/emote_data.json")) {
            old_emotes = JSON.parse(readFileSync("./saved/emote_data.json", {encoding:"utf-8"})); // Old emotes.

            for (let i = 0; i < emotes.length; i++) {
                formatted_emotes[(emotes[i].name).toString()] = 0;
            }

            // Check for deleted emotes:
            for (let i = 0; i < Object.keys(old_emotes).length; i++) {
                if (eval(`"${Object.keys(old_emotes)[i]}" in formatted_emotes`)) {
                    continue;
                } else {
                    this.deleted_emotes = this.deleted_emotes + `${Object.keys(old_emotes)[i]} `;
                }
            }

            for (let i = 0; i < this.deleted_emotes.split(' ').length; i++) {
                console.log(old_emotes[this.deleted_emotes.split(' ')[i]])
                delete old_emotes[this.deleted_emotes.split(' ')[i]];
            }
            
            // Check for new emotes:
            for (let i = 0; i < emotes.length; i++) {
                if (eval(`"${emotes[i].name}" in old_emotes`)) {
                    continue;
                } else {
                    old_emotes[(emotes[i].name).toString()] = 0;
                    this.new_emotes = this.new_emotes + `${emotes[i].name} `
                }
            }

            this.emotes = old_emotes;
            writeFileSync(`./saved/emote_data.json`, JSON.stringify(old_emotes,null,2), {encoding: "utf-8"});

            console.log("* 7TV channel emotes were loaded.");
            return old_emotes;
        }

        // Filling the json_emotes:
        for (let i = 0; i < emotes.length; i++) {
            formatted_emotes[emotes[i].name] = 0;
        }
        
        // Saving 7tv emotes with json_emotes:
        writeFileSync(`./saved/emote_data.json`, JSON.stringify(formatted_emotes, null, 2), {encoding: "utf-8"});
        
        console.log("* The 7tv emote file has been created and filled out!");
    }


}

module.exports = {EmoteUpdater};