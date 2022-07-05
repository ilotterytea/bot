// Copyright (C) 2022 ilotterytea
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
import ICommand from "./ICommand";
import ApolloConfiguration from "../../src/apollo/ApolloConfiguration";
import { ChatUserstate, Client } from "tmi.js";
import Localization from "../../src/apollo/utils/Localization";
import ApolloCfg from "../../src/apollo/ApolloConfiguration";

class Ping implements ICommand<String> {

    get getId(): string { return "ping"; }
    get getCooldownMs(): number { return 5000; }
    get getPermissions(): string[] { return ["mod"]; }

    async execute(args: ApolloCfg): Promise<String> {
        function formatTime(seconds: number) {
            function pad(s: number) { return (s < 10 ? "0" + s.toString() : s.toString()); }

            var days = Math.floor(seconds / (60 * 60 * 24));
            var hours = Math.floor(seconds / (60 * 60) % 24);
            var minutes = Math.floor(seconds % (60 * 60) / 60);
            var sec = Math.floor(seconds % 60);

            return `${days} d. ${pad(hours)}:${pad(minutes)}:${pad(sec)}`;
        }

        var mem = ``
        var uptime: string = formatTime(process.uptime());
        var rooms: number = args.storage.client_rooms.length;
        var dnkver: {} = args.DNKVER;

        return await Localization.parsedText(
            "c.ping.resp",
            args.user["room-id"],
            uptime, rooms.toString(), mem, dnkver
        );
    }
}

export default Ping;