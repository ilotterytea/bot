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

import { Logger } from "tslog";
import TwitchApi from "../clients/ApiClient";

const log: Logger = new Logger({name: "symlinks"});

class Symlinks {
    private symlinks: {[target_name: string]: string};
    private api: TwitchApi.Client;

    constructor (twitch_api: TwitchApi.Client) {
        this.api = twitch_api;
        this.symlinks = {};
    }

    public async register(target_id: string, target_name?: string | undefined): Promise<void> {
        if (target_name === undefined) {
            if (parseInt(target_id) < 0) return;
            const user = await this.api.getUserById(parseInt(target_id));
            if (user === undefined) return;
            target_name = user.login;
            
            log.debug("Successfully converted User ID", target_id, "to Username", user.login);
        }
        this.symlinks[target_name] = target_id;
    }

    public unregister(target_name: string): void {
        if (!this.containsSymlink(target_name)) return;
        delete this.symlinks[target_name];
    }

    public getSymlink(target_name: string): string | undefined {
        if (!this.containsSymlink(target_name)) return undefined;
        return this.symlinks[target_name];
    }

    public containsSymlink(target_name: string): boolean { return target_name in this.symlinks; }
    public getSymlinks(): {[target_name: string]: string} { return this.symlinks; }
}

export default Symlinks;