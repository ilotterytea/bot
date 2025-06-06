# Events

> This command is for broadcaster only

The `!event` command gives broadcasters the ability to manage events for streams and more.

> Events with types *category*, *message* and *title* use placeholders ({0}, {1}...) for values.
This means that the bot can show changes if you set them
(e.g. forsen changed the title from **{0}** to **{1}** will replace with
forsen changed the title from **Just Chatting** to **PUBG**).

## Event types
+ live
+ offline
+ title `({0} - previous title, {1} - new title)`
+ category `({0} - previous game, {1} - new game)`
+ message `({0} - sender name, {1} - channel name, {2} - message)`
+ custom

## Event flags
+ `massping` - Massping everyone in chat regardless of their subscription to the event.

## Syntax

### Create a new event
`!event on <name>:<type> <message...>`
+ `<name>` - Twitch username or event name *(custom type only)*.
+ `<type>` - [Event type](#event-types).
+ `<message>` - The message that will be sent with the event.

### Delete the event
`!event off <name>:<type>`
+ `<name>` - Twitch username or event name *(custom type only)*.
+ `<type>` - [Event type](#event-types).

### Flag/unflag the event
`!event flag <name>:<type> <flag>`
+ `<name>` - Twitch username or event name *(custom type only)*.
+ `<type>` - [Event type](#event-types).
+ `<flag>` - [Event flag](#event-flags).

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

### Flag/unflag the event
+ `!event flag forsen:live massping`

### Calling the event
+ `!event call forsen:live`

## Responses

### Creating a new event
+ `A new "forsen:live" event has been successfully created! It will send a message when the event occurs.`

### Deleting the event
+ `The "forsen:live" (ID ...) event has been successfully deleted!`

### Adding the flag to the event
+ `Flag "massping" is set for the "forsen:live" event.`

### Removing the flag from the event
+ `Flag "massping" has been removed from the "forsen:live" event.`

### Calling the event
+ `⚡ forsen live!` 

## Important notes

+ If the specified event name does not belong to a Twitch user,
the event type will automatically be considered ***custom***.

## Error handling

+ [Error 0: Not enough arguments](/wiki/error-codes#0)
+ [Error 2: Incorrect argument](/wiki/error-codes#2)
+ [Error 3: Insufficient rights](/wiki/error-codes#3)
+ [Error 11: Namesake creation](/wiki/error-codes#11)
+ [Error 12: Not found](/wiki/error-codes#12)
+ [Error 127: Something went wrong](/wiki/error-codes#127)
