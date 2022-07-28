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
import { readFileSync } from "fs";

const log: Logger = new Logger({name: "www-serverinit"});

async function ServerInit(opts: {[key: string]: string}, ssl_certificate: IConfiguration) {
    
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
            res.render("pages/home", {
                botn: "fembajbot"
            });
            /*res.render("pages/commands", {
                cmds: [
                    {
                        name: "Ping!",
                        namespace_id: "ping",
                        template: "!ping",
                        description: "Checking if it's alive, and a bunch of other data.",
                        options: "",
                        cooldown: "5 sec.",
                        notes: "",
                        role: "public",
                        examples: {
                            default: [
                                "!ping",
                                "lol"
                            ]
                        }
                    }
                ]
            });*/
        });

        App.get("/about", (req, res) => {
            res.render("pages/home", {
                botn: "fembajbot"
            });
        });

        App.get("/stats", (req, res) => {
            res.render("pages/home", {
                botn: "fembajbot"
            });
        });

        App.get("/me", (req, res) => {
            res.render("pages/home", {
                botn: "fembajbot"
            });
        });

        App.use(express.static(`${__dirname}/static`));
        if (opts.debug) {
            App.listen(8080, () => {
                log.debug("The bot's web server is running on port", "8080");
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