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
const express = require("express");
const { existsSync, readFileSync } = require("fs");
const { ApolloClient } = require("../apollo/ApolloClient");

const botapi = require("./routers/api");
const home = require("./routers/index");

class WebClient {
    constructor (apolloClient, port, directoryName) {
        this.port = port || 12906;
        this.directoryName = directoryName || `${__dirname}`;
        this.apolloClient = apolloClient.getClient();

        this.app = express();
    }

    async create() {
        
        this.app.use("/", home);
        this.app.use("/public", express.static(`${this.directoryName}/public`));
        this.app.listen(this.port, async () => console.log(`* API Server is running on port ${this.port}...`));
    }

    async dispose() {
    }
}

module.exports = {WebClient};