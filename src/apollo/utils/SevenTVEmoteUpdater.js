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
const SevenTV = require("7tv");
const { writeFileSync } = require("fs");
const ApolloLogger = require("./ApolloLogger");

class SevenTVEmoteUpdater {
    constructor (channels) {
        this.channels = channels;
        this.emotes = {};
        this.deleted_emotes = {};
        this.new_emotes = {};
    }

    async LoadEmotes(emotes) {
        this.emotes = emotes;
        this.STV = SevenTV();
    }

    async SaveEmotes() {
        ApolloLogger.debug("SevenTVEmoteUpdater", "7TV channel emotes were updated!");
    }

    async UpdateEmoteCounter(target, message) {
        var msga = message.split(' ');
        var channel = target.slice(1, target.length);

        for (let i = 0; i < msga.length; i++) {
            if (Object.keys(this.emotes[channel]).includes(msga[i])) {
                this.emotes[channel][msga[i]] += 1
            }
        }
    }

    async UpdateEmotes() {
        this.new_emotes = {};
        this.deleted_emotes = {};

        this.channels.forEach(async (value, index, array) => {
            let channel = value.slice(1, value.length);
            this.new_emotes[channel] = "";
            this.deleted_emotes[channel] = "";
            var announceNewEmotes = true;

            let api_emotes = await this.STV.fetchUserEmotes(channel);

            // If channel is new, don't announce new emotes/deleted emotes:
            if (!(channel in this.emotes)) {
                this.emotes[channel] = {};
                announceNewEmotes = false;
            }

            let formatted_api_emotes = [];

            for (let f = 0; f < api_emotes.length; f++) {
                formatted_api_emotes.push(api_emotes[f].name);
            }

            // Detecting new emotes:
            for (let j = 0; j < formatted_api_emotes.length; j++) {
                if (!(Object.keys(this.emotes[channel]).includes(formatted_api_emotes[j]))) {
                    if (announceNewEmotes) this.new_emotes[channel] = this.new_emotes[channel] += `${formatted_api_emotes[j]} `;
                    this.emotes[channel][formatted_api_emotes[j]] = 0;
                }
            }

            if (announceNewEmotes) {
                // Detecting deleted emotes:
                for (let d = 0; d < Object.keys(this.emotes[channel]).length; d++) {
                    if (!(formatted_api_emotes.includes(Object.keys(this.emotes[channel])[d]))) {
                        this.deleted_emotes[channel] = this.deleted_emotes[channel] += `${Object.keys(this.emotes[channel])[d]} `;
                    }
                }

                for (let k = 0; k < this.deleted_emotes[channel].split(' ').length; k++) {
                    delete this.emotes[channel][this.deleted_emotes[channel].split(' ')[k]];
                }
            }

            if ((index + 1) == array.length) {
                this.SaveEmotes();
            }
        });
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
}

module.exports = {SevenTVEmoteUpdater};