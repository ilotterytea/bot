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

/**
 * Credentials.
 * @since 2.2.0
 * @author NotDankEnough
 */
interface ICredentials {
    /**
     * Bot's username.
     */
    Username: string | undefined,

    /**
     * Bot's OAuth token from https://twitchapps.com/tmi.
     */
    Password: string | undefined,

    /**
     * Client ID from your Twitch Developers Application.
     */
    ClientID: string | undefined,

    /**
     * Client Secret from your Twitch Developers Application.
     */
    ClientSecret: string | undefined,

    /**
     * Access token when you're authenticating in your Twitch Developers Application.
     */
    AccessToken: string | undefined,

    /**
     * API key from server. Required for use the "!iu save <LINK>".
     * More info can be found here: https://hmmtodayiwill.ru/api/key
     */
    //IOKeyAPI?: string | undefined
}

export default ICredentials;