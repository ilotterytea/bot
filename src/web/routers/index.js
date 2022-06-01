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
const { readFileSync } = require("fs");
const router = express.Router();
require("dotenv").config({path: "./bot.env"});
const storage = JSON.parse(readFileSync("storage/storage.json", {encoding: "utf-8"}));

router.use(function timeLog(req, res, next) {
    console.log("Time: ", Date.now());
    next();
});
router.get("/", async (req, res) => {
    let table = ``
    const chatl = storage.stats.chat_lines
    const exec = storage.stats.executed_commands
    const tests = storage.stats.tests

    var body = ``
    res.send(body).status(200);
});

module.exports = router;