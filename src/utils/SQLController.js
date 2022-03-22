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

// Libraries:
const pg = require("pg");

class SQLController {
    constructor (host, port, user, password, database) {
        this.client = new pg.Pool({
            host: host,
            port: port,
            user: user,
            password: password,
            database: database
        });

        this.client.connect();
    }

    async getAllRows(table) {
        const res = await this.client.query(`SELECT * from ${table}`);
        console.log(res.rows);
        await this.client.end();
    }
    /*
    async createRow(table, key, value) {

    }

    async getRow(table, key) {

    }

    async deleteRow(table, key) {

    }

    async updateRow(table, key) {

    }*/
}

module.exports = {SQLController}