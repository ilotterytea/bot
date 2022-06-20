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

    constructor (storage, langDir) {
        this.langdir = langDir;
        this.storage = storage.preferred;
        this.directory = [];
        this.loaded_languages = {};
        this.preferred_users = {};
    }

    async LoadLanguageFiles() {
        this.directory = [];
        this.loaded_languages = {};

        this.directory = readdirSync(this.langdir);
        this.directory.forEach((value, index, array) => {
            this.loaded_languages[value.replace(".json", "")] = JSON.parse(readFileSync(`${this.langdir}/${value}`));
        });

        this.UpdateUserPreferrences();
    }

    async UpdateUserPreferrences() {
        this.preferred_users = {};

        Object.keys(this.storage).forEach(value => {
            this.preferred_users[value] = this.storage[value].lang;
        });
    }

    async PlainText(key_id, channel) {
        var language = "";
        var text = "";

        if (!(channel in this.preferred_users)) {
            language = "en_us";
        } else {
            language = this.preferred_users[channel];
        }

        text = this.loaded_languages[language][key_id].split(' ');

        return text;
    }

    async ParsedText(key_id, channel, ...args) {
        var language = "";
        var text = "";

        if (!(channel in this.preferred_users)) {
            language = "en_us";
        } else {
            language = this.preferred_users[channel];
        }

        text = this.loaded_languages[language][key_id].split(' ');

        text.forEach((value, i, array)=> {
            switch (true) {
                case (value.includes("%0%")):
                    text[i] = text[i].replace("%0%", args[0]);    
                    break;
                case (value.includes("%1%")):
                    text[i] = text[i].replace("%1%", args[1]);    
                    break;
                case (value.includes("%2%")):
                    text[i] = text[i].replace("%2%", args[2]);    
                    break;
                case (value.includes("%3%")):
                    text[i] = text[i].replace("%3%", args[3]);    
                    break;
                case (value.includes("%4%")):
                    text[i] = text[i].replace("%4%", args[4]);    
                    break;
                case (value.includes("%5%")):
                    text[i] = text[i].replace("%5%", args[5]);    
                    break;
                case (value.includes("%6%")):
                    text[i] = text[i].replace("%6%", args[6]);    
                    break;
                case (value.includes("%7%")):
                    text[i] = text[i].replace("%7%", args[7]);    
                    break;
                case (value.includes("%8%")):
                    text[i] = text[i].replace("%8%", args[8]);    
                    break;
                case (value.includes("%9%")):
                    text[i] = text[i].replace("%9%", args[9]);    
                    break;
                default:
                    break;
            }
        });

        return text.join(' ');
    }
}

module.exports = {TranslationManager}