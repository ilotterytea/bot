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

import { Client } from "tmi.js";
import ApolloLogger from "../utils/ApolloLogger";
import storage from "../../../storage/storage.json";
import { HelixUser } from "twitch";
import { HelixApiGroup } from "twitch/lib/API/Helix/HelixApiGroup";

/**
 * Creates, connects, and returns a Twitch client.
 * @author NotDankEnough.
 * @param username Bot's username.
 * @param password Bot's OAuth token.
 * @param api A created instance of the Twitch API.
 * @param isInDebugMode Enable the debug mode? If true, the bot will only join his chat room.
 * @returns Tmi.js client.
 */
function ApolloClient(
    username: string,
    password: string,
    api: HelixApiGroup,
    isInDebugMode?: boolean
) {

    // Creating a new Twitch client:
    const client = new Client({
        options: { debug: isInDebugMode },
        connection: { reconnect: true, secure: true },
        identity: {
            username: username,
            password: password
        },
        channels: [username]
    });

    // Connect the Twitch client:
    client
        .connect()
        .catch((err) => {
            ApolloLogger.error("ApolloClient", err);
        })
        .then(async () => {
            if (!isInDebugMode) {
                storage.join.asclient.forEach(async (value) => {
                    (await api).users
                        .getUserById(value)
                        .then((user: HelixUser | null) => {
                            if (!user) return false;
                            client.join(`#${user?.name}`);
                        }, (reason: string) => {
                            ApolloLogger.error("ApolloClient", "Promise is rejected: ", reason);
                        });
                });
            }
        });
    
    // Listeners:
    client.on("connected", (address: string, port: number) => ApolloLogger.debug("ApolloClient", `The client (${username}) (${address}:${port})`));
    client.on("disconnected", (reason: string) => ApolloLogger.debug("ApolloClient", `The client (${username}) (${reason})`));
    client.on("reconnect", () => ApolloLogger.debug("ApolloClient", `The client (${username}) `));

    return client;
}

export default ApolloClient;