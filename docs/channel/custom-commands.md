# Custom commands

> This command is for broadcaster only

The `!cmd` command gives broadcasters the ability to create their own chat commands.

## Syntax

### Create a new custom command
`!cmd new <name> <message...>`
+ `<name>` - The name for new custom command. It should be unique for your chat.
**A prefix must be specified if you want a prefixed command, e.g. `!sub`, `!server`.**
+ `<message>` - Text that will be sent when the custom command is invoked. By the way, you can run a command within a command by writing a command request as you would normally write **(but with the prefix "!")**. See [Usage](#usage) for more details.

### Delete the custom command
`!cmd delete <name>`
+ `<name>` - Name of custom command.

### Edit the message for custom command
`!cmd message <name> <message...>`
+ `<name>` - Name of custom command.
+ `<message>` - Text with which to replace

### Toggle (enable/disable) the custom command
`!cmd toggle <name>`
+ `<name>` - Name of custom command.

### Check the information about custom command
`!cmd info <name>`
+ `<name>` - Name of custom command

### Make a custom command available in all chats *(requires owner rights)*
`!cmd global <name>`
+ `<name>` - Name of custom command

### Get the list of created custom commands
`!cmd list`

## Usage

### Creating a new custom command
+ `!cmd new !sub Buy a Twitch sub at this link and become like the rest of us 😎`
+ `!cmd new !forsen !spam 5 forsen` - This will create the `!forsen` command, which will invoke the `!spam 5 forsen` command on demand.

### Deleting the custom command
+ `!cmd delete !sub`

### Editing the message for custom command
+ `!cmd message !sub Buy a Prime sub at this link and become like the rest of us 😎`

### Toggling the custom command
+ `!cmd toggle !sub`

### Checking the information about the custom command
+ `!cmd info !sub`

## Responses

### Creating a new custom command
+ `A new custom command with "!sub" name has been successfully created!`

### Deleting the custom command
+ `The "!sub" (ID ...) custom command has been deleted!`

### Editing the message for custom command
+ `The message for "!sub" (ID ...) custom command has been changed!`

### Toggling the custom command
+ If the command was enabled: `The "!sub" (ID ...) custom command has been disabled!`
+ If the command was disabled: `The "!sub" (ID ...) custom command has been enabled!`

### Checking the information about the custom command
+ `✅ !sub (ID ...) | Message: Buy a Prime sub at this link and become like the rest of us 😎`

## Error handling

+ [Error 0: Not enough arguments](/wiki/error-codes#0)
+ [Error 11: Namesake creation](/wiki/error-codes#11)
+ [Error 12: Not found](/wiki/error-codes#12)
+ [Error 127: Something went wrong](/wiki/error-codes#127)
