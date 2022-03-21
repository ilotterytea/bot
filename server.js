var express = require("express");
var app = express();
const TwitchApi = require("node-twitch").default;
require("dotenv").config({path: "./default.env"});
const {readFileSync, existsSync} = require("fs");

const api = new TwitchApi({
    client_id: process.env.TTV_CLIENTID,
    client_secret: process.env.TTV_CLIENTSECRET
});

// Page
app.get("/", async (req, res) => {
    const username = req.query.username;
    const user = await api.getUsers(username);
    console.log(user);

    if (username != undefined) {
        try {
            res.send(`<!DOCTYPE html>
            <html lang="en">
                <head>
                    <meta charset="utf-8"> <!-- Charset -->
                    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> <!-- Charset -->
                    <title>iLotterybot2 - ${req.query.username}</title> <!-- Title -->
                    <meta name="description" content=""> <!-- Description -->
                    <meta name="author" content="iLotterytea"> <!-- Author -->
                    <meta name="viewport" content="width=device-width,initial-scale=1"> <!-- Viewport -->
                    <link rel="stylesheet" href="/css/style.css"> <!-- Stylesheet -->
                    <link rel="shortcut icon" href="${user.data[0].profile_image_url}"> <!-- Favicon -->
                    <script type="module" src="https://cdn.jsdelivr.net/gh/zerodevx/zero-md@2/dist/zero-md.min.js"></script>
                </head>
                <body>
                    <div id="container">
                        <header class="clearfix">
                            <div id="mojang-bar-container">
                
                            </div>
                        </header>
                        <div id="main" role="main">
                            <a href="index.html" id="logo"></a>
                            <noscript>
                                <div id="javascript-warning">
                                    Please enable JavaScript to use this site.
                                </div>
                            </noscript>
                            <div class="clearfix" id="data_page">
                                <div style="float:left; width:366px;">
                                    <h2>Your key to all Twitch messages in one click</h2>
                                    <p>Enter Twitch username in the "username" field, and the bot will show any information <i>(chat logs, public user data)</i> it has.<br></p>
                                    <form>
                                        <p class="field_text">
                                            <label>Username</label>
                                            <input id="username" class="" type="text" name="username" value="">
                                        </p>
                                        <p id="signin-field">
                                            <button id="loginSubmit" class="huge button" onclick="">Search user</button>
                                        </p> 
                                    </form>
                                    
                                </div>
                                <div style="float:left; width:544px; height:340px; margin-left:50px; background:transparent url(/img/emote.webp) no-repeat center;">
                                </div>
                                
                                <div id="twitch-profile" style="color: #ffffff;">
                                    <div class="profile" style="background: url(${user.data[0].offline_image_url}) no-repeat center fixed;background-size: cover;">
                                        <div class="profile-left">
                                            <img src="${user.data[0].profile_image_url}" width=128 title="${user.data[0].display_name}'s profile picture" alt="${user.data[0].display_name}'s profile picture">
                                        </div>
                                        <div class="profile-right">
                                            <h2>About ${user.data[0].display_name} <span style="color:#bebebe">(<span title="User Name" style="cursor:help;">${user.data[0].login}</span>, <span title="User ID" style="cursor:help;">${user.data[0].id}</span>)</span></h2>
                                            <div>
                                                <span style="font-weight: bold;">${user.data[0].view_count}</span> views • <span title="Data creation" style="cursor:help;">${user.data[0].created_at}</span>${user.data[0].broadcaster_type != "" ? ` • ${user.data[0].broadcaster_type}` : ``}${user.data[0].type != "" ? ` • ${user.data[0].type}` : ``}
                                                <p title="Description">${user.data[0].description}</p>
                                            </div>
                                        </div>
                                    </div><!--
                                    <h2 style="color:#000000;">${user.public_data.display_name}'s last message: </h2>
                                    <div class="message">
                                        <p><span style="color:#979797"><span title="Timestamp" style="cursor:help;">${user.last_seen.timestamp}</span> <span title="Channel" style="cursor:help;">${user.last_seen.channel}</span></span> ${(user.public_data.badges.includes("prime")) ? `<img src="https://static-cdn.jtvnw.net/badges/v1/bbbe0db0-a598-423e-86d0-f9fb98ca1933/1"> ` : ``} ${(user.public_data.badges.includes("chatterino_supporter")) ? `<img src="https://fourtf.com/chatterino/badges/supporter.png"> ` : ``} <span style="color:${user.public_data.color}">${user.public_data.display_name}:</span> ${user.last_seen.msg} </p>
                                    </div>
                                    <div id="bot_info" style="color: #696969">
                                        <i>First time registered: ${user.bot.first_time_registered} • Detected on channels: ${user.bot.detected_on_channels} • NotDankEnough's Alias ID: ${user.bot_alias_id} • <a href="/request?form=hide&username=${user.public_data.username}">Request to hide ${user.public_data.username}</a></i>
                                    </div>-->
                                </div>
                            </div>
                        </div>
                    </div>
                </body>
            </html>`);
        } catch (err) {
            console.error(err);
            res.sendFile(`${__dirname}/src/tearino/404.html`);
        }
    }

    res.sendFile(`${__dirname}/src/tearino/index.html`);
});

app.get("/request", (req, res) => {
    if (existsSync(`${__dirname}/saved/pubdata/${req.query.username}.json`)) {
        try {
            require(`${__dirname}/src/tearino/request.js`).request(req, res);
        } catch (err) {
            console.error(err);
            res.redirect("/");
        }
    } else {
        res.redirect("/");
    }
    
});

app.use(express.static(`${__dirname}/public`)); // Load "assets" folder.
app.listen(3000, () => console.log(`App listening on port 3000!`)); // Launch the web app.