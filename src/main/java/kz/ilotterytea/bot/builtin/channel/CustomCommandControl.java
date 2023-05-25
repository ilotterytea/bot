package kz.ilotterytea.bot.builtin.channel;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.CustomCommand;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.ParsedMessage;

import org.hibernate.Session;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 'Custom command control' command.
 * @author ilotterytea
 * @since 1.1
 */
public class CustomCommandControl implements Command {
    @Override
    public String getNameId() { return "cmd"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return Collections.singletonList("no-mention"); }

    @Override
    public List<String> getSubcommands() { return List.of("new", "edit", "delete", "rename", "copy", "toggle", "list"); }

    @Override
    public List<String> getAliases() { return List.of("scmd", "custom", "command", "команда"); }

    @Override
    public Optional<String> run(Session session, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
    	if (message.getMessage().isEmpty()) {
    		return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_MESSAGE
            ));
    	}
    	
        ArrayList<String> s = new ArrayList<>(List.of(message.getMessage().get().split(" ")));

        if (message.getSubcommandId().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_SUBCMD
            ));
        }

        if (message.getSubcommandId().get().equals("list")) {
            if (channel.getCommands().isEmpty()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_CMD_NOCMDS,
                        channel.getAliasName()
                ));
            }

            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_CMD_SUCCESS_LIST,
                    channel.getAliasName(),
                    channel.getCommands().stream().map(CustomCommand::getName).collect(Collectors.joining(", "))
            ));
        }

        if (Objects.equals(s.get(0), "")) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NOT_ENOUGH_ARGS
            ));
        }


        final String name = s.get(0);
        s.remove(0);
        
        // If the command was run by a broadcaster:
        if (permission.getPermission().getValue() >= Permission.BROADCASTER.getValue()) {
            Optional<CustomCommand> optionalCustomCommands = channel.getCommands().stream().filter(c -> c.getName().equals(name)).findFirst();
            String response = String.join(" ", s);

            if (Objects.equals(response, "")) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_CMD_NOSECONDARG
                ));
            }

            // Create a new custom command:
            if (message.getSubcommandId().get().equals("new")) {
                // Check if a command with the same name already exists:
                if (optionalCustomCommands.isPresent() || Huinyabot.getInstance().getLoader().getCommand(name).isPresent()) {
                    return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_CMD_ALREADYEXISTS,
                            name
                    ));
                }

                // Creating a new command and assign it to the channel:
                CustomCommand command = new CustomCommand(name, response, channel);
                channel.addCommand(command);

                // Saving changes:
                session.persist(channel);
                session.persist(command);

                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_CMD_SUCCESS_NEW,
                        command.getName()
                ));
            }

            // If the command not exists:
            if (optionalCustomCommands.isEmpty()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_CMD_DOESNOTEXISTS,
                        name
                ));
            }

            CustomCommand command = optionalCustomCommands.get();

            switch (message.getSubcommandId().get()) {
                // "Edit a command response" clause:
                case "edit": {
                    // Setting a new response:
                    command.setMessage(response);

                    // Saving changes:
                    session.persist(command);

                    return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_CMD_SUCCESS_EDIT,
                            command.getName()
                    ));
                }
                // "Delete a command" clause:
                case "delete":
                    // Deleting a command and saving changes:
                    session.remove(command);

                    return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_CMD_SUCCESS_DELETE,
                            command.getName()
                    ));
                case "rename":
                    String nameToRename = s.get(0);
                    String previousName = command.getName();

                    System.out.println(nameToRename);
                    System.out.println(previousName);

                    command.setName(nameToRename);

                    // Saving changes:
                    session.persist(command);

                    return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_CMD_SUCCESS_RENAME,
                            previousName,
                            nameToRename
                    ));
                default:
                    break;
            }
        }

        return Optional.empty();
    }
}
