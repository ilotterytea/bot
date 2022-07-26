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

import IArguments from "../../interfaces/IArguments";
import EventSource from "eventsource";

namespace EmoteUpdater {
    export class SevenTV {
        private targets: string[];
        private link: string;
        private src: EventSource;

        constructor (targets: string[]) {
            this.targets = targets;
            this.link = "";
            this.targets.forEach((target, index) => {
                if (index == 0) {
                    this.link = this.link + "?channel=" + target;
                } else {
                    this.link = this.link + "&channel=" + target;
                }
            });

            this.src = new EventSource("https://events.7tv.app/v1/channel-emotes" + this.link);
        }

        subscribe(args: IArguments) {
            this.src.addEventListener(
                "ready",
                (e) => {
                  // Should be "7tv-event-sub.v1" since this is the `v1` endpoint
                  console.log(e.data);
                }
              );
              
              this.src.addEventListener(
                "update",
                (e) => {
                  // This is a JSON payload matching the type for the specified event channel
                  var data: {[key: string]: any} = JSON.parse(e.data);

                  console.log(data);

                  switch (data.action) {
                    case "ADD":
                        args.client.action(`#${data.channel}`, args.localizator.parsedText("emoteupdater.user_added_emote", data.channel, "[7TV]", data.actor, data.name));
                        break;
                    case "REMOVE":
                        args.client.action(`#${data.channel}`, args.localizator.parsedText("emoteupdater.user_deleted_emote", data.channel, "[7TV]", data.actor, data.name));
                        break;
                    case "UPDATE":
                        args.client.action(`#${data.channel}`, args.localizator.parsedText("emoteupdater.user_updated_emote", data.channel, "[7TV]", data.actor, data.emote.name, data.name));
                        break;
                    default:
                        break;
                  }
                }
              );
              
              this.src.addEventListener(
                "open",
                (e) => {
                  // Connection was opened.
                }
              );
              
              this.src.addEventListener(
                "error",
                (e) => {
                }
              );
        }
    }
}

export default EmoteUpdater;