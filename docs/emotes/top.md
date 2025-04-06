# Emote usage leaderboard

The `!etop` command provides the ability to find out the top emotes by usage in a specified chat room.
This is a useful tool for those who want to keep track of the popularity and frequency of use of specific emotes in the community.

## Syntax

`!etop <ttv/bttv/ffz/7tv/all> <channel/me>`
+ `<ttv/bttv/ffz/7tv>` (optional) - Parameter to sort emotes by their providers.
The default setting is **all**.
+ `<channel/me>` (optional) - Parameter to show channel's top emotes or your's.
the default setting is **channel**.

## Usage

+ `!etop` - Shows the top 5 of all emotes in the current channel.
+ `!etop bttv` - Shows the top 5 of BTTV emotes in the current channel.
+ `!etop me` - Shows your top 5 of all emotes.
+ `!etop bttv me` - Shows your top 5 of BTTV emotes.

## Responses
+ `forsen's top 5 of all emotes: TriHard (1210), sadE (1011), EZY (1000)...`
+ `forsen's top 5 of BTTV emotes: forsenHoppedIn (750), FailFors (600), forsenGravity (599), ...`
+ `ilotterytea's top 5 of all emotes: BAND (121), sadE (80), LULW (79), ...`
+ `ilotterytea's top 5 of Twitch emotes: RlyTho (50), DatSheffy (35), TriHard (31), ...`

## Important notes

+ Emote information is stored and retrieved from an external
["ilotterytea/stats"](https://stats.ilotterytea.kz) service.
+ Emotes data may be temporarily unavailable if a bot has just joined a chat.

## Error handling

+ [Error 12: Not found](/wiki/error-codes#12)
+ [Error 127: Something went wrong](/wiki/error-codes#127)
