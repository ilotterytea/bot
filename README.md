<!-- HEADER -->
<div align="center">
        <h2><img src="https://cdn.frankerfacez.com/emote/675001/1" style="vertical-align:middle;" width="22"> iLotterytea's Twitch Bot 2 (formerly ilotterybot2)
        <br>
        <img src="https://wakatime.com/badge/user/09f67b1c-0691-482a-a1d4-e4751e6962de/project/c3f899b4-ca47-46c7-9838-3548f0a9546f.svg?style=plastic">
        <img src="https://img.shields.io/github/license/notdankenough/itb2?style=plastic">
        <img src="https://img.shields.io/github/package-json/v/notdankenough/itb2?style=plastic">
        </h2>
</div>

<!-- DESCRIPTION -->
Full reworking of [ilotterybot](https://github.com/notdankenough/ilotterybot). It uses JavaScript instead of Python, as in the legacy version.
+ [TO-DO List](https://github.com/NotDankEnough/iLotteryteaLive/projects/1)
+ [Website](https://bot.hmmtodayiwill.ru/)

## Dependicies:
- nodejs
- npm
- ts-node
<!-- INSTALLATION GUIDE -->
## Installation guide:
1. Run these commands in the terminal:
```bash
$ git clone --recurse-submodules https://github.com/notdankenough/itb2.git
$ cd itb2
$ npm install
```
2. Initialize the first steps of bot. It will create configuration files:
```bash
$ npm run firststeps
```
3. 
$ npm run bot
```
<!-- $ sudo chmod +x ./index.js
$ ./index.js --install -->
2. When done, the bot should create the necessary directories and files. To completely complete the installation, create a file `bot.env` in the root of the itb2 and copypaste the following text:
```env
TTV_USERNAME = "" # Insert the username of your Twitch bot.

TTV_PASSWORD = "" # OAuth Token of your Twitch bot. Can be obtained here: https://twitchapps.com/tmi/

TTV_CLIENT = "" # Client ID from your Twitch application, which you can register here: https://dev.twitch.tv/

TTV_SECRET = "" # The Сlient Secret is under the Client ID field on Twitch Developer page. This field is not currently used in any area of the code.

TTV_TOKEN = "" # You can get the token if you authorize at this link, replacing <CLIENT_ID> with the one you specified 2 lines above: https://id.twitch.tv/oauth2/authorize?response_type=token&redirect_uri=http://localhost:8080/auth&client_id=<CLIENT_ID>. When you're authorized, your search box should look like this: http://localhost:8080/#access_token=z5bmlx261bhtpqt3y4lraylailkur5&token_type=bearer. Copy the value of `#access_token` and paste it here.
```
3. Enter the information you need in `storage/storage.json`, such as the channel list *(be sure to enter the user ID, not its username)*.
3. Start the bot (in normal mode) again:
```bash
$ npm run bot
```
4. ???
5. PROFIT! <img src="https://cdn.7tv.app/emote/617539105ff09767de2a221c/1x" style="vertical-align:middle;">