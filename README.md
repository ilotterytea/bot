# ilotterytea's rustpilled bot

A utility and entertainment multi-chat Twitch bot.

## Features

+ Listening to the stream start/end and notifying chatters.
+ Sending messages in intervals *(like timers)*
+ PostgreSQL database support
+ Ping all chatters with a message *(massping)*
+ Running user-created Lua scripts
+ Counting emote usage. [More...](#more-about-emote-usage-counting)

## Prerequisites

+ Rust compiler
+ PostgreSQL
+ Diesel CLI `(cargo install diesel_cli --no-default-features -F postgres)`

## Installation guide

1. Install all prerequisites.
2. Clone the repository from the desired source and open it in the terminal.
3. [Create a configuration file *(rustpilled_bot.toml)*](#rustpilled_bottoml-example-with-all-available-parameters)
4. Applying database migration: `diesel migration run --database-url "postgres://user:pass@host/name"`

5. Run the bot

+ Twitch bot: `cargo run --release --package bot`
+ API: `cargo run --release --package api`

### rustpilled_bot.toml example with all available variables

```toml
[database]
url = "postgres://user:pass@host/name"

[bot]
username = "YYYYYY"
password = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
owner_twitch_id = 123456789 # optional, can be used by some commands to access superuser subcommands.
client_id = "ABCDEF" # optional, used by web auth.
client_secret = "FEDCBA"  # optional, used by web auth.
redirect_uri = "ACEBDF"  # optional, used by web auth.

[web]
port = 8080 # optional
contact_name = "forsen" # optional
contact_url = "https://twitch.tv/forsen" # optional
bot_title = "forsen's twitch bot" # optional

[commands]
default_prefix = "!" # optional
default_language = "english" # optional

[commands.spam]
max_count = 50 # optional, maximum number of lines that !spam can send at a time

[third_party]
docs_url = "https://forsen.tv/wiki" # optional, this is base url for command reference
stats_api_url = "https://stats.forsen.tv" # optional
stats_api_password = "TZULQS" # optional, required if you want to join your channels there.
pastea_api_url = "https://paste.forsen.tv" # optional
pastea_api_password = "ASDASD" # optional, can be used by !chatters to post authorized pastes, otherwise they will be “posted by anonymous”.
```

> You must generate an OAuth2 password from [TwitchTokenGenerator with special scopes](https://twitchtokengenerator.com/quick/riIPG7o2Fd) for bot, because this password is used not only for chat communication, but also for commands that use Twitch API endpoints that can only be accessed with special permissions *(for example, !massping requires moderator:read:chatters)*.

### More about emote usage counting

You can use [standard instance of "ilotterytea/stats"](https://stats.ilotterytea.kz), but you will only be able to get statistics from channels that [teabot] has joined. If you want to join your own channels, you need to [deploy your own instance of "ilotterytea/stats"](https://github.com/ilotterytea/stats) and set the `stats_api_url` and `stats_api_password` variables in the `rustpilled_bot.toml` file.