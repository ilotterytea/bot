# Stream events

> This command is for broadcaster only

The `!event` command gives broadcasters the ability to manage events for streams.

## Event types
+ live
+ offline
+ title
+ category
+ custom

## Syntax

### Create a new event
`!event on <name>:<type> <message...>`
+ `<name>` - Twitch username or event name *(custom type only)*.
+ `<type>` - [Event type](#event-types).
+ `<message>` - The message that will be sent with the event.

> Events with types *category* and *title* use *{0}* and *{1}* placeholders for old and new values respectively. This means that the bot can show changes if you set them (e.g. *forsen changed the title from **{0}** to **{1}*** will replace with *forsen changed the title from **Just Chatting** to **PUBG***).

### Delete the event
`!event off <name>:<type>`
+ `<name>` - Twitch username or event name *(custom type only)*.
+ `<type>` - [Event type](#event-types).

### Call the event

> The bot requires moderator privileges on events with the **"massping"** flag.

`!event call <name>:<type>`
+ `<name>` - Twitch username or event name *(custom type only)*.
+ `<type>` - [Event type](#event-types).

## Usage

### Creating a new event
+ `!event on forsen:live forsen live!`

### Deleting the event
+ `!event off forsen:live`

### Calling the event
+ `!event call forsen:live`

## Responses

### Creating a new event
+ `A new "forsen:live" event has been successfully created! It will send a message when the event occurs.`

### Deleting the event
+ `The "forsen:live" (ID ...) event has been successfully deleted!`

### Calling the event
+ `⚡ forsen live!` 

## Important notes

+ If the specified event name does not belong to a Twitch user,
the event type will automatically be considered ***custom***.

## Error handling

+ [Error 0: Not enough arguments](/help/errors#0)
+ [Error 2: Incorrect argument](/help/errors#2)
+ [Error 3: Insufficient rights](/help/errors#3)
+ [Error 11: Namesake creation](/help/errors#11)
+ [Error 12: Not found](/help/errors#12)
+ [Error 127: Something went wrong](/help/errors#127)
