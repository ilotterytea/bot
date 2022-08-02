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

import { Logger } from "tslog";
import express from "express";
import http from "http";
import https from "https";
import IConfiguration from "../apollo/interfaces/IConfiguration";
import { readdirSync, readFileSync } from "fs";
import StoreManager from "../apollo/files/StoreManager";
import TwitchApi from "../apollo/clients/ApiClient";
import IStorage from "../apollo/interfaces/IStorage";

const log: Logger = new Logger({name: "www-serverinit"});

async function ServerInit(opts: {[key: string]: string}, storage: StoreManager, ttvapi: TwitchApi.Client, ssl_certificate: IConfiguration) {
    
    try {
        const App = express();
        App.set("view engine", "ejs");
        App.set("views", `${__dirname}/views`);

        App.get("/", (req, res) => {
            res.render("pages/home", {
                botn: "fembajbot"
            });
        });

        App.get("/commands", (req, res) => {
            const cmds: {[key: string]: any} = JSON.parse(readFileSync("www/static/metadata/cmds.json", {encoding: "utf-8"}));

            res.render("pages/commands", {
                cmds: cmds
            });
        });

        App.get("/commands/:namespaceId", (req, res) => {
            const cmds: {[key: string]: any} = JSON.parse(readFileSync("www/static/metadata/cmds.json", {encoding: "utf-8"}));

            if (!(req.params.namespaceId in cmds)) {
                res.render("pages/error", {
                    status: 404,
                    message: "The command with ID " + req.params.namespaceId +" does not exist.",
                    kitty: "https://http.cat/404"
                });
                return;
            }
            if ("isHidden" in cmds[req.params.namespaceId]) {
                if (cmds[req.params.namespaceId].isHidden == true) {
                    res.render("pages/error", {
                        status: 401,
                        message: "The command with ID " + req.params.namespaceId +" is hidden from public view.",
                        kitty: "https://http.cat/401"
                    });
                    return;
                }
            }

            res.render("pages/commandpage", {
                cmd: cmds[req.params.namespaceId],
                cmds: cmds,
                bot_name: "fembajbot"
            });
        });

        App.get("/about", (req, res) => {
            res.render("pages/home", {
                botn: "fembajbot"
            });
        });

        App.get("/catalogue", async (req, res) => {
            if (req.query.s == undefined) { req.query.s = "target"; }
            if (req.query.s == "target") {
                const users: any[] = [];

                for await (const target of Object.keys(storage.targets.getTargets)) {
                    await ttvapi.getUserByName(storage.targets.getTargets[target].Name!).then(async (user) => {
                        if (user === undefined) return false;
                        users.push(user);
                    });
                }
                console.log(users);
                res.render("pages/catalogue", {
                    users: users,
                    bot_name: "fembajbot"
                });
                return;
            }
            
        });

        App.get("/channel/:id", async (req, res) => {
            // usernames:
            if (isNaN(parseInt(req.params.id))) {
                var found: boolean = false;
                for (const target of Object.keys(storage.targets.getTargets)) {
                    if (req.params.id.toLowerCase() == storage.targets.getTargets[target].Name!) {
                        req.params.id = target;
                        found = true;
                    }
                }

                if (!found) {
                    res.render("pages/error", {
                        status: 404,
                        message: "Target with username " + req.params.id +" not found.",
                        kitty: "https://http.cat/404"
                    });
                    return;
                }
            }

            // ids:
            if (!(req.params.id in storage.targets.getTargets) && /^[0-9].*$/.test(req.params.id)) {
                res.render("pages/error", {
                    status: 404,
                    message: "Target with ID " + req.params.id +" not found.",
                    kitty: "https://http.cat/404"
                });
                return;
            }

            var itb2data: IStorage.Target = storage.targets.getTargets[req.params.id];
            var ttvdata = (isNaN(parseInt(req.params.id))) ? await ttvapi.getUserByName(req.params.id) : await ttvapi.getUserById(parseInt(req.params.id));

            res.render("pages/channel", {
                itb: itb2data,
                ttv: ttvdata
            });
        });

        App.get("/me", (req, res) => {
            res.render("pages/home", {
                botn: "fembajbot"
            });
        });

        App.use(express.static(`${__dirname}/static`));
        if (opts.debug) {
            http.createServer(App).listen(8080, () => {
                log.debug("The bot web server is running on port", "8080");
            });
        } else {
            var credentials = {
                key: readFileSync(ssl_certificate.Web.Private, {encoding: "utf-8"}),
                cert: readFileSync(ssl_certificate.Web.Certificate, {encoding: "utf-8"}),
                ca: readFileSync(ssl_certificate.Web.Chain, {encoding: "utf-8"})
            };

            http.createServer(App).listen(80, () => {
                log.debug("The bot's web HTTP server is running on port", "80");
            });
            
            https.createServer(credentials, App).listen(443, () => {
                log.debug( "The bot's web HTTPS server is running on port", "443");
            });
        }
    } catch (err) {
        log.error(err);
    }
}

export default ServerInit;