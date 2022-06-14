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

const ttv = require("twitch");
const ttva = require("twitch-auth")

class TwitchGQL {
    constructor (clientId, token) {
        this.StaticAuthProvider = new ttva.StaticAuthProvider(clientId, token);
        this.GQL = new ttv.ApiClient({authProvider: this.StaticAuthProvider});
    }

    async getNamesByIds(ids) {
        const userids = await this.GQL.helix.users.getUsersByIds(ids);
        let usernames = [];

        userids.forEach(async (value, index, array) => {
            if (!userids[index]) {
                return false;
            }
            usernames.push(userids[index].name);
        });

        return usernames;
    }

    async getUserById(id) {
        const user = await this.GQL.helix.users.getUserById(id);
        if (!user) return false;
        user.id
        return user;
    }

    async getUserByName(name) {
        const user = await this.GQL.helix.users.getUserByName(name);
        if (!user) return false;
        return user;
    }

    async isValidUserByName(name) {
        const user = await this.GQL.helix.users.getUserByName(name);
        if (!user) return false;
        return true;
    }

    async isValidUserById(id) {
        const user = await this.GQL.helix.users.getUserById(id);
        if (!user) return false;
        return true;
    }
}

module.exports = {TwitchGQL}