# ilotterytea's rustpilled bot

A utility and entertainment multi-chat Twitch bot.

## Features

+ Listening to the stream start/end and notifying chatters.
+ Sending messages in intervals *(like timers)*
+ PostgreSQL database support
+ Ping all chatters with a message *(massping)*
+ [Counting emote usage](#enabling-emote-usage-counting-optional)

## Installation guide

### Clone the git repository

```bash
git clone https://git.ilotterytea.kz/tea/bot.git
cd bot
```

### Create the configuration file

```env
POSTGRES_USER=db_user
POSTGRES_DB=db_name
POSTGRES_PASSWORD=db_password
POSTGRES_HOSTNAME=db

BOT_USERNAME=YYYYYY
BOT_PASSWORD=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

> You must generate an OAuth2 password from [TwitchTokenGenerator with special scopes](https://twitchtokengenerator.com/quick/riIPG7o2Fd) for bot, because this password is used not only for chat communication, but also for commands that use Twitch API endpoints that can only be accessed with special permissions *(for example, !massping requires moderator:read:chatters)*.

> If you are going to use Docker, then **POSTGRES_HOSTNAME** must be equal to **db** *(POSTGRES_HOSTNAME=db)*

> If you are going to run it yourself *(via cargo run)*, **POSTGRES_HOSTNAME** must equal **localhost or IP address if the server is not local**
*(e.g. POSTGRES_HOSTNAME=localhost)*.

### Startup

1. Via Docker Compose

```bash
docker-compose up
```

> The installation will take up about 10-11 gigabytes of space.

2. Manually

+ Twitch bot: `cargo run --release --package bot`
+ API: `cargo run --release --package api`
+ Web: `cd web && npm run build && npm start`

### Enabling emote usage counting (optional)
1. Clone the git repository of [ilotterytea/stats](https://git.ilotterytea.kz/tea/stats)

```bash
git clone https://git.ilotterytea.kz/tea/stats.git
cd stats
```

2. Follow the installation steps in [the ilotterytea/stats's README file](https://git.ilotterytea.kz/tea/stats/src/branch/master/README.md)

3. Add these fields to the bot's configuration

```env
STATS_API_HOSTNAME=XXXXXX
STATS_API_PASSWORD=AAAAAA:BBBBBB
```

> `STATS_API_PASSWORD` is optional unless you set reverse proxy authentication for the `/join` and `/part` endpoints mentioned in [the stats's README file](https://git.ilotterytea.kz/tea/stats/src/branch/master/README.md).