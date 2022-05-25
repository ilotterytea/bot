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

const { readdirSync, readFileSync } = require("fs");

class TranslationManager {
    constructor (preferredlangchannels, langs_directory) {
        this.channels = preferredlangchannels;
        this.langs_directory = langs_directory;
        this.language_names = readdirSync(langs_directory);

        this.languages = {};
        this.attached_to_languages = {};
    }

    async LoadLanguages() {
        this.language_names.forEach(async (value, index, array) => {
            this.languages[value.replace(".json", '')] = JSON.parse(readFileSync(`${this.langs_directory}/${value}`, {encoding: "utf-8"}));
        });
        
        Object.keys(this.channels).forEach(async (value, index, array) => {
            this.attached_to_languages[value] = this.channels[value].lang
        });
    }  

    async TranslationKey(key, args, customvariables) {
        const text = eval(`this.languages[this.attached_to_languages[args.target.slice(1, args.target.length)]].${key}`);
        return this.replaceVariables(text, args, customvariables);
    }

    async replaceVariables(key, args, customvariables) {
        var splittedkey = key.split(" ");

        for (let i = 0; i < splittedkey.length; i++) {
            switch(true) {
                case (splittedkey[i].includes("%user%")):
                    splittedkey[i] = splittedkey[i].replace("%user%", args.user.username);
                    break;
                case (splittedkey[i].includes("%emotes%")):
                    splittedkey[i] = splittedkey[i].replace("%emotes%", args.emotes);
                    break;
                case (splittedkey[i].includes("%0%")):
                    splittedkey[i] = splittedkey[i].replace("%0%", customvariables[0])
                    break;
                case (splittedkey[i].includes("%1%")):
                    splittedkey[i] = splittedkey[i].replace("%1%", customvariables[1])
                    break;
                default:
                    break;
            }
        }

        return splittedkey.join(' ');
    }

    async getTranslationKeysByTarget(target) {
        return this.languages[this.attached_to_languages[target.slice(1, target.length)]];
    }

    async getNotFilteredTranslationKey(key, args) {
        return eval(`this.languages[this.attached_to_languages[args.target.slice(1, args.target.length)]].${key}`);
    }
}

module.exports = {TranslationManager}