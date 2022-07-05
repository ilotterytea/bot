// Copyright 2022 ilotterytea
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import { readFileSync } from "fs";

namespace StoreManager {
    export interface IStorage {
        Version: string | undefined
    }

    export class Storage {
        storage: any;
        file_path: string;

        constructor (file_path: string) {
            this.storage = JSON.parse(readFileSync(file_path, {encoding: "utf-8"}));
            this.file_path = file_path;
        }

        getStoreData() {
           var data: IStorage = {
            Version: "v1"
           };

           return data;
        }
    }
}

export default StoreManager;