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

const log: Logger = new Logger({name: "TmiApiClient"});

namespace TwitchApi {
    interface HelixUser {
        id: string,
        login: string,
        display_name: string,
        type: string,
        broadcaster_type: string,
        description: string,
        profile_image_url: string,
        offline_image_url: string,
        view_count: number,
        email?: string,
        created_at: string
    }

    export class Client {
        private client_id: string;
        private access_token: string;
    
        constructor(client_id: string, access_token: string) {
            this.client_id = client_id;
            this.access_token = access_token;
        }
    
        async getUserById(user_id: number) {
            var user: HelixUser | undefined;
    
            await axios.get(
                "https://api.twitch.tv/helix/users?id=" + user_id.toString(),
                {
                    responseType: "json",
                    headers: {
                        "Authorization": "Bearer " + this.access_token,
                        "Client-Id": this.client_id
                    }
                }
            ).then((response) => {
                user = response.data.data[0];
            }).catch((reason) => {
                log.error(reason);
            });
    
            return user;
        }
    }
}

export default TwitchApi;