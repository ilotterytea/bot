# Holiday

The `!holiday` command allows you to get information about today's and upcoming holidays.

> **So far, only Russian holidays and in Russian.**

## Syntax

`!holiday <date>`
+ `<date>` (Optional) - The date on which you want to get a random holiday. \
If it's not specified, today's date will be used.

## Usage

+ `!holiday` - Get today's random holiday.
+ `!holiday 25.02` - Get a random holiday on February 25th.
+ `!holiday 25` - Get a random holiday on the 25th of this month.
+ `!holiday .02` - Get a random holiday for today's date in February *(e.g. if today is the 1st, then it's February 1st)*.
+ `!holiday yesterday` - Get yesterday's random holiday.
+ `!holiday tommorow` - Get tommorow's random holiday.

## Response

+ `Holiday for 25.02 (3/8): День открытия спирта`

## Important notes

+ The information is obtained from the third-party service ["ilotterytea/holidays"](https://hol.ilotterytea.kz)

## Error handling

+ [Error 3: Incorrect argument](/help/errors#3)
+ [Error 20: External API error](/help/errors#20)
+ [Error 127: Something went wrong](/help/errors#127)
