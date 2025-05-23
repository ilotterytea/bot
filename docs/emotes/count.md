# Check the usage of emote

The `!ecount` command is designed to track the number of times an emote has been used in a chat.
This feature allows users to find out how many times a certain emote has been used in messages.
To use the command, you must specify the name of the emote after the command.

## Syntax

`!ecount <name>`
+ `<name>` - The name of the emote.

## Usage

+ `!ecount forsenHoppedIn`
+ `!ecount DankPoke`
+ `!ecount Okayeg`
+ `!ecount :)`

## Responses

+ `BTTV Emote forsenHoppedIn has been used 1337 times`
+ `7TV Emote DankPoke has been used 33 times`
+ `FFZ Emote Okayeg has been used 37 times`
+ `TTV Emote :) has been used 13 times`

## Important notes

+ Emote information is stored and retrieved from an external
["ilotterytea/stats"](https://stats.ilotterytea.kz) service.
+ Emotes data may be temporarily unavailable if a bot has just joined a chat.

## Error handling

+ [Error 0: Not enough arguments](/wiki/error-codes#0)
+ [Error 12: Not found](/wiki/error-codes#12)
