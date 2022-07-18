// Copyright (C) 2022 NotDankEnough (ilotterytea)
// 
// This file is part of itb2.
// 
// itb2 is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// itb2 is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with itb2.  If not, see <http://www.gnu.org/licenses/>.

import axios from "axios";
import { Logger } from "tslog";
import IStorage from "../interfaces/IStorage";

const log: Logger = new Logger({name: "Localizator"});

type LineIds = "user.not_found" | "arrive" | "leave" | "newarrive" |
"test.test" | "terminal.error" | "emoteupdater.new_emotes" |
"emoteupdater.deleted_emotes" | "cmd.help.name" | "cmd.help.desc" |
"cmd.help.author" | "cmd.help.exec.response" | "cmd.help.exec.help" |
"cmd.help.exec.lolresponse" | "cmd.ping.name" | "cmd.ping.desc" |
"cmd.ping.author" | "cmd.ping.exec" | "cmd.ping.author" |
"cmd.ping.exec.response" | "cmd.ecount.name" | "cmd.ecount.desc" |
"cmd.ecount.author" | "cmd.ecount.exec.response" | "cmd.ecount.not_enough_params" |
"cmd.etop.name" | "cmd.etop.desc" | "cmd.etop.author" |
"cmd.etop.exec.response" | "cmd.etop.exec.not_enough_emotes" | "cmd.join.name" |
"cmd.join.desc" | "cmd.join.author" | "cmd.join.exec.response" |
"cmd.join.exec.already_in" | "cmd.set.name" | "cmd.set.desc" |
"cmd.set.author" | "cmd.set.exec.usage" | "cmd.set.exec.languagesetup.response" |
"cmd.set.exec.languagesetup.available" | "cmd.massping.name" | "cmd.massping.desc" |
"cmd.massping.author" | "cmd.massping.exec.usage" | "cmd.massping.exec.response" |
"cmd.spam.name" | "cmd.spam.desc" | "cmd.spam.author" |
"cmd.spam.exec.usage" | "cmd.spam.exec.response" | "cmd.spam.exec.unauthorized" |
"cmd.storage.name" | "cmd.storage.desc" | "cmd.storage.author" |
"cmd.storage.exec.prefix.now" | "cmd.storage.exec.prefix.changed" | "cmd.storage.exec.group.not_found" |
"cmd.storage.exec.group.changed" | "cmd.storage.exec.part.successfully" | "cmd.storage.exec.part.not_in_join_list" |
"cmd.scmd.name" | "cmd.scmd.desc" | "cmd.scmd.author" |
"cmd.scmd.exec.list.response" | "cmd.scmd.exec.list.no" | "cmd.scmd.exec.make.success" |
"cmd.scmd.exec.make.failure" | "cmd.scmd.exec.rm.success" | "cmd.scmd.exec.rm.failure" |
"cmd.scmd.exec.ch.success" | "cmd.scmd.exec.ch.failure" | "measure.megabyte";

class Localizator {
    private languages: {[lang_id: string]: {[line_id: string]: LineIds}} | undefined;
    private preferred_langs: {[lang_id: string]: string[]};

    constructor () {
        this.languages = {};
        this.preferred_langs = {};
    }

    setPreferredLanguages(raw_targets: {[target_id: string]: IStorage.Target}) {
        Object.keys(this.languages!).forEach((lang_id) => {
            Object.keys(raw_targets).forEach((target_id) => {
                if (raw_targets[target_id].LanguageId === lang_id) {
                    if (!(lang_id in this.preferred_langs)) this.preferred_langs[lang_id] = [];
                    this.preferred_langs[lang_id].push(target_id);
                }
            });
        });
    }

    async loadLanguages(noInternetSync: boolean, custom_languages?: {[lang_id: string]: {[line_id: string]: LineIds}}) {
        if (noInternetSync) {
            this.languages = custom_languages;
            log.debug("Loaded languages provided by the user!");
            return;
        }

        var raw: boolean = false;
        await axios.get("https://hmmtodayiwill.ru/api/assets/languages" + ((raw) ? "/?raw=true" : ""), {
            responseType: "json"
        }).then((response) => {
            if (response.status != 200) {
                log.warn("Got status from iLotterytea's API:", response.status);
                return;
            }
            if (raw) {
                log.warn("Raw JSON languages aren't supported yet.");
                return;
            }
            console.log(response.data);
            this.languages = response.data;
        }).catch((err) => log.error(err));
        log.silly("dun");
    }

    get getLanguages() { return this.languages; }
    get getAvailableLangs() { return Object.keys(this.languages!); }
    get getPreferredLangs() { return this.preferred_langs; }

    parsedText(line_id: LineIds, target_id: string, ...args: any[]) {
        var lang_id: string = "en_us";

        Object.keys(this.preferred_langs).forEach((lang_id2) => {
            if (this.preferred_langs[lang_id2].includes(target_id)) {
                lang_id = lang_id2;
            }
        });

        if (!(lang_id in this.languages!)) {
            log.warn("Language with", lang_id, "code doesn't exist! Setting the en_us language...");
            delete this.preferred_langs[lang_id];
            lang_id = "en_us";
            this.preferred_langs[lang_id].push(target_id);
        }

        var message: string = this.replaceDummyValues(this.languages![lang_id][line_id], args);

        return message;
    }

    private replaceDummyValues(text: string, ...args: any[]) {
        var _text = text.split(' ');

        _text.forEach((value, index) => {
            switch (true) {
                case (value.includes("%0%")):
                    _text[index] = _text[index].replace("%0%", args[0][0]);
                    break;
                case (value.includes("%1%")):
                    _text[index] = _text[index].replace("%1%", args[0][1]);
                    break;
                case (value.includes("%2%")):
                    _text[index] = _text[index].replace("%2%", args[0][2]);
                    break;
                case (value.includes("%3%")):
                    _text[index] = _text[index].replace("%3%", args[0][3]);
                    break;
                case (value.includes("%4%")):
                    _text[index] = _text[index].replace("%4%", args[0][4]);
                    break;
                case (value.includes("%5%")):
                    _text[index] = _text[index].replace("%5%", args[0][5]);
                    break;
                case (value.includes("%6%")):
                    _text[index] = _text[index].replace("%6%", args[0][6]);
                    break;
                case (value.includes("%7%")):
                    _text[index] = _text[index].replace("%7%", args[0][7]);
                    break;
                case (value.includes("%8%")):
                    _text[index] = _text[index].replace("%8%", args[0][8]);
                    break;
                case (value.includes("%9%")):
                    _text[index] = _text[index].replace("%9%", args[0][9]);
                    break;
                default:
                    break;
            }
        });

        return _text.join(' ');
    }

    addPreferredUser(lang_id: string, target_id: string) {
        if (!(lang_id in this.preferred_langs)) this.preferred_langs[lang_id] = [];

        Object.keys(this.preferred_langs).forEach((lang_id2) => {
            if (this.preferred_langs[lang_id2].includes(target_id)) {
                this.preferred_langs[lang_id2] = this.preferred_langs[lang_id2].filter(t => t !== target_id);
            }
        });

        this.preferred_langs[lang_id].push(target_id);
    }

    removePreferredUser(target_id: string) {
        Object.keys(this.preferred_langs).forEach((lang_id) => {
            if (this.preferred_langs[lang_id].includes(target_id)) {
                this.preferred_langs[lang_id] = this.preferred_langs[lang_id].filter(t => t !== target_id);
            }
        });
    }
}

export default Localizator;