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

const { readFileSync } = require("fs");
const TwitchApi = require("node-twitch").default;
require("dotenv").config({path: "./default.env"});

const api = new TwitchApi({
    client_id: process.env.TTV_CLIENTID,
    client_secret: process.env.TTV_CLIENTSECRET
});

exports.inspect = async (request,response) => {
    //const user = JSON.parse(readFileSync(`././saved/pubdata/${request.query.username}.json`, {encoding: "utf-8"}));
    const user = await api.getUsers(request.query.username).then((user) => {response.send()});

/*
    let stri = ``

    for (let i = 0; i < user.public_data.moderator.length; i++) {
        stri = stri += `<a href="https://twitch.tv/${user.public_data.moderator[i].split('#')[1]}" target="_blank">${user.public_data.moderator[i].split('#')[1]}</a>${i == (user.public_data.moderator.length - 1) ? `` : `, `}`
    }

    let stri2 = ``

    for (let i = 0; i < user.public_data.subscriber.length; i++) {
        stri2 = stri2 += `<a href="https://twitch.tv/${user.public_data.subscriber[i].split('#')[1]}" target="_blank">${user.public_data.subscriber[i].split('#')[1]}</a>${i == (user.public_data.subscriber.length - 1) ? `` : `, `}`
    }
    */
};