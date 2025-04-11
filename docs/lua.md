# Lua coding

As of version 1.1, ilotterytea's twitch bot allows you to run custom scripts written in Lua in Twitch chats. The scripts are run in the [Luau sandbox](https://luau.org/sandbox) (in simpler terms, Roblox's Lua). This is a safe environment that prevents malicious code from doing its bad things by removing access to certain libraries.

Also, there are execution limits for scripts - 2MB RAM and 500 milliseconds wait time. These limits are made for cases where you may have forgotten "while-true loop" and so it doesn't run indefinitely until the bot shuts down, the loop will stop after 500 milliseconds. Additionally, it's a test to see if you know how to write optimized code :)

The bot provides [API calls](#list-of-api-calls) for your scripts, which will be expanded in the future, and [commands](#commands) that allow you to run Lua scripts.

If you are interested in any examples, there are [a mini-game about drinking 'not milk'](https://paste.ilotterytea.kz/milkE) and [internal Lua commands](https://github.com/ilotterytea/bot/tree/master/modules).

Have fun!

## Commands

### Syntax

#### Lua script in chat

`!lua <code...>`

+ `<code>` - Lua-validated code.

#### Remote Lua script

`!luaimport <provider>:<id>`

+ `<provider>` - A paste-like service. Available services: **pastebin (Pastebin)**, **pastea [(Rustpastes)](https://paste.ilotterytea.kz)**.
+ `<id>` - An ID of the paste.

### Usage

+ `!lua 2+2`
+ `!lua function hello(name) return "hello, " .. name end return hello("chat")`
+ `!luaimport pastebin:Ejdrqmb7`

### Responses

+ `🌑 4`
+ `🌑 hello, chat`
+ `🌑 hello ilotterytea! you just ran a remote script`

### Error handling

+ [Error 0: Not Enough Arguments](/wiki/error-codes#0)
+ [Error 2: Incorrect Argument](/wiki/error-codes#2)
+ [Error 20: External API Error](/wiki/error-codes#20)
+ [Error 31: Lua Execution Error](/wiki/error-codes#31)
+ [Error 32: Lua Unsupported Response Type](/wiki/error-codes#32)
+ [Error 33: Lua Exceeded Waiting Time](/wiki/error-codes#33)
+ [Error 127: Something Went Wrong](/wiki/error-codes#127)

## List of API calls

### Localization

+ `l10n_formatted_text_request(request, line_id, parameters) -> String` - Get translated, formatted text.

### Time

+ `time_current() -> Integer` - Get current UTC time in milliseconds.
+ `time_humanize(timestamp) -> String` - Convert UNIX timestamp to humanized timestamp *(e.g. 5m10s, 25y09mo)*

### Storage `(!luaimport only)`

+ `storage_get() -> String` - Get user's storage cell. Returns an empty string if it was just created.
+ `storage_put(string) -> Bool` - Put string to user's storage cell. Returns true if it was successful.
+ `storage_channel_get() -> String` - Get channel's storage cell. Returns an empty string if it was just created.
+ `storage_channel_put(string) -> Bool` - Put string to channel's storage cell. Returns true if it was successful.

### JSON

+ `json_parse(string) -> Table` - Convert stringified JSON to valid Lua value.
+ `json_stringify(Any) -> String` - Convert Lua value to stringified JSON

### String

+ `str_split(value: String, separator: String) -> String[]` - Split a string by the delimiter.

### Bot

+ `err(error_id: String, arguments: Table) -> Error` - Generate an [error](#error).
+ `bot_config() -> BotConfiguration` - Get [bot configuration](#bot_configuration).
+ `bot_get_compiler_version() -> String` - Get Rust compiler version.
+ `bot_get_uptime() -> Integer` - Get bot uptime in milliseconds.
+ `bot_get_memory_usage() -> Integer` - Get current memory usage in bytes.
+ `bot_get_compile_time() -> Integer` - Get compilation timestamp in seconds.
+ `bot_get_version() -> String` - Get bot version.

## List of structs

### Request

```
Request {
    command_id: String,
    subcommand_id: String | Nil,
    message: String | Nil,
    sender: Sender {
        id: Integer,
        alias_id: Integer,
        alias_name: String,
        joined_at: Integer,
        opted_out_at: Integer | Nil
    },
    channel: Channel {
        id: Integer,
        alias_id: Integer,
        alias_name: String,
        joined_at: Integer,
        opted_out_at: Integer | Nil
    },
    channel_preference: ChannelPreference {
        id: Integer,
        channel_id: Integer,
        prefix: String,
        language: String,
        features: (String | Nil)[]
    },
    rights: Rights {
        id: Integer,
        user_id: Integer,
        channel_id: Integer,
        level: String,
        is_fixed: Bool
    }
}
```

### Error

```
Error {
    type: String,
    name: String,
    arguments: String[]
}
```

### Bot configuration

```
BotConfiguration {
    bot: {
        owner_twitch_id: Integer | nil
    },
    commands: {
        default_prefix: String,
        default_language: String
        spam: {
            max_count: Integer
        }
    },
    third_party: {
        docs_url: String,
        stats_api_url: String,
        pastea_api_url: String
    }
}
```