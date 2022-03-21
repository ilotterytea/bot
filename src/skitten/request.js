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

exports.request = async (request, response) => {
    const body = `<!DOCTYPE html>
    <!-- HTML Start: -->
    <html lang="en">
        
        <!-- HTML Head: -->
        <head>
            <meta charset="utf-8"> <!-- Charset -->
            <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> <!-- Charset -->
            <title>iLotterybot2 - ${request.query.username}</title> <!-- Title -->
            <meta name="description" content=""> <!-- Description -->
            <meta name="author" content="iLotterytea"> <!-- Author -->
            <meta name="viewport" content="width=device-width,initial-scale=1"> <!-- Viewport -->
            <link rel="stylesheet" href="/css/style.css"> <!-- Stylesheet -->
            <link rel="shortcut icon" href=""> <!-- Favicon -->
            <script type="module" src="https://cdn.jsdelivr.net/gh/zerodevx/zero-md@2/dist/zero-md.min.js"></script>
        </head>
        
        <!-- HTML Body: -->
        <body style="">
            <!-- Container: -->
            <div id="container">
                
                <!-- Page header: -->
                <header class="clearfix">
                    <div id="mojang-bar-container">
        
                    </div>
                </header>
                
                <!-- Main page. -->
                <div id="main" role="main">
                    
                    <!-- Logo. -->
                    <a href="index.html" id="logo"></a>
    
                    <!-- If JavaScript is disabled: -->
                    <noscript>
                        <div id="javascript-warning">
                            Please enable JavaScript to use this site.
                        </div>
                    </noscript>
                    
                    <!-- Data page: -->
                    <div class="clearfix" id="data_page">
                        <!-- Left: -->
                        <div style="float:left; width:366px;">
                            <h2>Request to hide ${request.query.username}'s public data and chat logs.</h2>
                            <p>Enter Twitch username in the "username" field, and the bot will show any information <i>(chat logs, public user data)</i> it has.<br></p>
                            <form>
                                <p class="field_text">
                                    <label>Username</label>
                                    <input id="username" class="" type="text" name="username" value="${request.query.username}">
                                </p>
                                <p class="field_text">
                                    <label>Message (optional)</label>
                                    <textarea id="message" name="message" form="form" value="">
                                </p>
                                <p id="signin-field">
                                    <button id="loginSubmit" class="huge button" onclick="">Send</button>
                                </p> 
                            </form>
                            
                        </div>
                    </div>
                </div>
            </div>
        </body>
    </html>`
            
    

    return response.send(body)
};