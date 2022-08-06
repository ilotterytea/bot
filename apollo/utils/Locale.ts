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
import IArguments from "../interfaces/IArguments";
import IStorage from "../interfaces/IStorage";
import LineIds from "../types/LineIds";
import os from "node-os-utils";
import git from "git-rev-sync";
import pck from "../../package.json";
import { existsSync, readdirSync, readFileSync } from "fs";

const log: Logger = new Logger({name: "Localizator"});

class Localizator {
    private languages: {[lang_id: string]: {[line_id: string]: string}} | undefined;
    private preferred_langs: {[lang_id: string]: string[]};
    private user_links: {[target_name: string]: string};

    constructor () {
        this.languages = {};
        this.user_links = {};
        this.preferred_langs = {};
    }

    public load(localization_file: string) {
        this.languages = {};

        if (!existsSync(localization_file)) {
            return new Error("Localization file (" + localization_file + ") doesn't exists!");
        }

        const lang: {[line_id: string]: {[lang_id: string]: string}} = JSON.parse(readFileSync(localization_file, {encoding: "utf-8"}));

        for (const line_id of Object.keys(lang)) {
            for (const lang_id of Object.keys(lang[line_id])) {
                if (!(lang_id in this.languages)) {
                    this.languages[lang_id] = {};
                }

                this.languages[lang_id][line_id] = lang[line_id][lang_id];
            }
        }
    }

    setPreferredLanguages(raw_targets: {[target_id: string]: IStorage.Target}, user_links: {[target_name: string]: string}) {
        Object.keys(this.languages!).forEach((lang_id) => {
            Object.keys(raw_targets).forEach((target_id) => {
                if (raw_targets[target_id].LanguageId === lang_id) {
                    if (!(lang_id in this.preferred_langs)) this.preferred_langs[lang_id] = [];
                    this.preferred_langs[lang_id].push(target_id);
                }
            });
        });
        this.user_links = user_links;
    }

    get getLanguages() { return this.languages; }
    get getAvailableLangs() { return Object.keys(this.languages!); }
    get getPreferredLangs() { return this.preferred_langs; }

    /**
     * Get the line ID with replaced placeholders.
     * @param line_id Line ID to replace.
     * @param args Arguments. Without arguments, the function will return a text
     * @param options Options to parse.
     * @param optional_args Optional arguments.
     */
    public parsedText(line_id: LineIds, args?: IArguments | undefined, optional_args?: any[], options?: {
        useUsername?: boolean | undefined
    }) {
        // If languages aren't loaded -> return string with error.
        if (this.languages === undefined) {
            return "Line ID " + line_id + " can't be defined because languages aren't loaded.";
        }

        // Language ID.
        var lang_id: string = "en_us";
        var message: string | undefined = "";

        // If arguments isn't set:
        if (args === undefined) {
            return this.languages[lang_id][line_id];
        }

        // Use username instead of user ID. Used in 7TV EventAPI notices.
        var useUsername: boolean = (options !== undefined && options.useUsername !== undefined) ? options.useUsername : false;

        // Set the user ID on useUsername:
        if (useUsername) {
            if (!(args.Target.Username in this.user_links)) {
                log.warn("Username", args.Target.Username, "not found in this.user_links.");
                return "";
            }
            args.Target.Username = this.user_links[args.Target.Username];
        }

        // Getting the user's preferred language:
        for (const l_id of Object.keys(this.preferred_langs)) {
            if (this.preferred_langs[l_id].includes(args.Target.ID)) {
                lang_id = l_id;
            }
        }

        // Replacing the placeholders:
        message = this.replaceDummyValues(this.languages[lang_id][line_id], args, lang_id, optional_args);
        
        if (message === undefined) {
            message = "";
        }

        return message;
    }

    /**
     * Get the custom text with replaced placeholders.
     * @param text Text to replace.
     * @param args Arguments.
     * @param options Options to parse.
     * @param optional_args Optional arguments.
     */
    public customParsedText(text: string, args: IArguments, ...optional_args: any[]) {
        var message: string | undefined = "";

        // Replacing the placeholders:
        message = this.replaceDummyValues(text, args, "en_us", optional_args);
        
        if (message === undefined) {
            message = "";
        }

        return message;
    }

    private replaceDummyValues(text: string, args?: IArguments | undefined, lang_id?: string | undefined, optional_args?: any[]) {
        if (text === undefined) return;
        
        var _text = text.split(' ');

        if (lang_id === undefined) {
            lang_id = "en_us"
        }

        if (optional_args === undefined) optional_args = [];

        for (const word of _text) {
            if (args !== undefined) {
                switch (true) {
                    // Targets:
                    case word.includes("${TARGET.NAME}"): {
                        _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${TARGET.NAME}", args.Target.Username);
                        break;
                    }
                    case word.includes("${TARGET.ID}"): {
                        _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${TARGET.ID}", args.Target.ID);
                        break;
                    }
    
                    // User:
                    case word.includes("${USER.NAME}"): {
                        _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${USER.NAME}", args.Sender.Username);
                        break;
                    }
                    case word.includes("${USER.ID}"): {
                        _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${USER.ID}", args.Sender.ID);
                        break;
                    }
                    
                    // Channel:
                    case word.includes("${CHANNELS.CLIENT.LENGTH}"): {
                        _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${CHANNELS.CLIENT.LENGTH}", args.Services.Storage.Global.getClientJoin.length.toString());
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            
            switch (true) {
                // Uptime:
                case word.includes("${UPTIME}"): {
                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${UPTIME}", process.uptime().toString());
                    break;
                }
                case word.includes("${UPTIMEF}"): {
                    function formatTime(seconds: number) {
                        function pad(s: number) {
                            return (s < 10 ? '0' : '') + s.toString();
                        }
            
                        var days = Math.floor(seconds / (60 * 60 * 24));
                        var hours = Math.floor(seconds / (60 * 60) % 24);
                        var minutes = Math.floor(seconds % (60 * 60) / 60);
                        var sec = Math.floor(seconds % 60);
            
                        return `${days} d. ${pad(hours)}:${pad(minutes)}:${pad(sec)}`;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${UPTIMEF}", formatTime(process.uptime()));
                    break;
                }
                // Package.json:
                case word.includes("${PCK.NAME}"): {
                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${PCK.NAME}", pck.name);
                    break;
                }
                case word.includes("${PCK.VERSION}"): {
                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${PCK.VERSION}", pck.version);
                    break;
                }

                // Git:
                case word.includes("${GIT.SHORT}"): {
                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${GIT.SHORT}", git.short());
                    break;
                }
                case word.includes("${GIT.BRANCH}"): {
                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${GIT.BRANCH}", git.branch());
                    break;
                }
                case word.includes("${GIT.MESSAGE}"): {
                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${GIT.MESSAGE}", git.message());
                    break;
                }
                
                // Languages:
                case word.includes("${LANGUAGES.AVAILABLE}"): {
                    if (this.languages === undefined) return;
                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${LANGUAGES.AVAILABLE}", Object.keys(this.languages).join(', '));
                    break;
                }

                // Optional arguments:
                case word.includes("${0}"): {
                    if (optional_args.length === 0) {
                        log.warn("The word includes tag ${0}, but optional arguments length equals to zero.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${0}", optional_args[0]);
                    break;
                }
                case word.includes("${1}"): {
                    if (optional_args.length < 1) {
                        log.warn("The word includes tag ${1}, but optional arguments length less than 1.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${1}", optional_args[1]);
                    break;
                }
                case word.includes("${2}"): {
                    if (optional_args.length < 2) {
                        log.warn("The word includes tag ${2}, but optional arguments length less than 2.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${2}", optional_args[2]);
                    break;
                }
                case word.includes("${3}"): {
                    if (optional_args.length < 3) {
                        log.warn("The word includes tag ${3}, but optional arguments length less than 3.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${3}", optional_args[3]);
                    break;
                }
                case word.includes("${4}"): {
                    if (optional_args.length < 4) {
                        log.warn("The word includes tag ${4}, but optional arguments length less than 4.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${4}", optional_args[4]);
                    break;
                }
                case word.includes("${5}"): {
                    if (optional_args.length < 5) {
                        log.warn("The word includes tag ${5}, but optional arguments length less than 5.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${5}", optional_args[5]);
                    break;
                }
                case word.includes("${6}"): {
                    if (optional_args.length < 6) {
                        log.warn("The word includes tag ${6}, but optional arguments length less than 6.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${6}", optional_args[6]);
                    break;
                }
                case word.includes("${7}"): {
                    if (optional_args.length - 1 < 7) {
                        log.warn("The word includes tag ${7}, but optional arguments length less than 7.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${7}", optional_args[7]);
                    break;
                }
                case word.includes("${8}"): {
                    if (optional_args.length - 1 < 8) {
                        log.warn("The word includes tag ${8}, but optional arguments length less than 8.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${8}", optional_args[8]);
                    break;
                }
                case word.includes("${9}"): {
                    if (optional_args.length - 1 < 9) {
                        log.warn("The word includes tag ${9}, but optional arguments length less than 9.");
                        return;
                    }

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("${9}", optional_args[9]);
                    break;
                }

                // Other:
                case word.includes("@{MEASURE.MEGABYTES}"): {
                    if (this.languages === undefined) return;

                    _text[_text.indexOf(word)] = _text[_text.indexOf(word)].replace("@{MEASURE.MEGABYTES}", this.languages[lang_id]["measure.megabytes"]);
                    break;
                }
                default: {
                    break;
                }
            }
        }

        return _text.join(' ');
    }

    addPreferredUser(lang_id: string, target_id: string) {
        if (!(lang_id in this.preferred_langs)) this.preferred_langs[lang_id] = [];

        Object.keys(this.preferred_langs).forEach((lang_id2) => {

            this.preferred_langs[lang_id2].forEach((user) => {
                if (user.includes(target_id)) {
                    this.preferred_langs[lang_id2] = this.preferred_langs[lang_id2].filter(t => t !== user);
                }
            });
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