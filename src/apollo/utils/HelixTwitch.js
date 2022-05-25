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

const { StaticAuthProvider, ApiClient } = require("twitch/lib");

class TwitchGQL {
    constructor (clientId, token) {
        this.StaticAuthProvider = new StaticAuthProvider(clientId, token);
        this.GQL = new ApiClient({th});
    }

    async getUsernamesById(ids) {
        let usernames = [];
        ids.forEach(async (value) => {
            const user = await this.GQL.helix.users.getUsersByIds()
            if (!user) {
                return false;
            }
            usernames.push(user.name);
        });
        return usernames;
    }
    async isValidUserByUsername(username) {
        const user = await this.GQL.helix.users.getUserByName(username);
        if (!user) {
            return false;
        }
        return true;
    }
    async isValidUserById(id) {
        const user = await this.GQL.helix.users.getUserById(id);
        if (!user) {
            return false;
        }
        return true;
    }

    async getFormattedUserDataByUsername(username) {
        const user = await this.GQL.helix.users.getUserByName(username);
        if (!user) {
            return false;
        }
        return 
    }
}

module.exports = {TwitchGQL}