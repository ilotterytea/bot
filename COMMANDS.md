# Commands

| ID | Description                                                                                 | Author(s) | Options                                                                                                                                                            | Subcommands                            | Aliases | Minimal requirements |
|----|---------------------------------------------------------------------------------------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|---------|----------------------|
| ping | Checking if it's alive, and a bunch of other data.                                          | iLotterytea | 🚫                                                                                                                                                                 | 🚫                                     | `pong`, `пинг`, `понг` |  Everyone. |
| holiday | Check today's holidays. The Russian site is used, so these holidays are in Russian.  | iLotterytea, GreDDySS, Fedotir | !massping with today's holiday: `--massping, --тык`; Get all the holidays: `--all, --все`; Don't replace the username with the emote: `--no-emotes, --без-эмоутов` | 🚫                                     | `праздник` | Everyone. |
| massping | Ping em, Fors! 💪 ![LUL](https://static-cdn.jtvnw.net/emoticons/v2/425618/default/dark/1.0) | iLotterytea | 🚫                                                                                                                                                                 | 🚫                                     | `mp`, `масспинг`, `massping` | ![Broadcaster](https://static-cdn.jtvnw.net/badges/v1/5527c58c-fb7d-422d-b71b-f309dcb85cc1/1) |
| spam | Spam the message a certain number of times.                                                 | iLotterytea | Show the count: `--count`.                                                                                                                                         | The spam count: `ANY NUMBER (exactly)` | `repeat`, `cvpaste`, `cv`, `paste`, `насрать`, `спам` | ![Moderator](https://static-cdn.jtvnw.net/badges/v1/3267646d-33f0-4b17-b3df-f923a41db1d0/1) |

## Usage
To use these commands, you must send a message to a chat room that has Huinyabot with this pattern: `![ID OR ALIAS] [OPTIONS..., SUBCOMMAND...] ...`.
It does not matter where you place the options and subcommands. Of course, you should not send with square brackets or multiple dots. **You must also use the "--" prefix before the option itself to use options!**
### Examples:
+ `!holiday --massping`
+ `!праздник --тык` *(the same as above, but with aliases)*
+ `!spam 6 Hello, world! --count` *("6" is how much to spam)*