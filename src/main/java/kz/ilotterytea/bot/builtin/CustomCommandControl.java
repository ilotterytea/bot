package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.entities.CustomCommand;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.utils.HibernateUtil;
import org.hibernate.Session;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 'Custom command control' command.
 * @author ilotterytea
 * @since 1.1
 */
public class CustomCommandControl extends Command {
    @Override
    public String getNameId() { return "cmd"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Arrays.asList("no-mention")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(Arrays.asList("new", "edit", "delete", "rename", "copy", "toggle", "list")); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("scmd", "custom", "command", "команда")); }

    @Override
    public String run(ArgumentsModel m) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        // Getting local info about the channel:
        List<Channel> channels = session.createQuery("from Channel where aliasId = :aliasId", Channel.class)
                .setParameter("aliasId", m.getEvent().getChannel().getId())
                .getResultList();

        Channel channel = channels.get(0);

        // Getting local info about the user:
        List<User> users = session.createQuery("from User where aliasId = :aliasId", User.class)
                .setParameter("aliasId", m.getEvent().getUser().getId())
                .getResultList();

        User user = users.get(0);

        // Getting info about the permission:
        List<UserPermission> permissions = session.createQuery("from UserPermission where user = :user AND channel = :channel", UserPermission.class)
                .setParameter("user", user)
                .setParameter("channel", channel)
                .getResultList();

        UserPermission permission = permissions.get(0);

        ArrayList<String> s = new ArrayList<>(Arrays.asList(m.getMessage().getMessage().split(" ")));

        if (m.getMessage().getSubCommand() == null) {
            return Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_SUBCMD
            );
        }

        if (m.getMessage().getSubCommand().equals("list")) {
            if (channel.getCommands().isEmpty()) {
                return Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_CMD_NOCMDS,
                        channel.getAliasName()
                );
            }

            return Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_CMD_SUCCESS_LIST,
                    channel.getAliasName(),
                    channel.getCommands().stream().map(CustomCommand::getName).collect(Collectors.joining(", "))
            );
        }

        if (Objects.equals(s.get(0), "")) {
            return Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NOT_ENOUGH_ARGS
            );
        }


        final String name = s.get(0);
        s.remove(0);

        // If the command was run by a broadcaster:
        if (permission.getPermission().getValue() >= Permissions.BROADCASTER.getId()) {
            Optional<CustomCommand> optionalCustomCommands = channel.getCommands().stream().filter(c -> c.getName().equals(name)).findFirst();
            String response = String.join(" ", s);

            if (Objects.equals(response, "")) {
                return Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_CMD_NOSECONDARG
                );
            }

            // Create a new custom command:
            if (m.getMessage().getSubCommand().equals("new")) {
                // Check if a command with the same name already exists:
                if (optionalCustomCommands.isPresent() || Huinyabot.getInstance().getLoader().getCommand(name).isPresent()) {
                    return Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_CMD_ALREADYEXISTS,
                            name
                    );
                }

                // Creating a new command and assign it to the channel:
                CustomCommand command = new CustomCommand(name, response, channel);
                channel.addCommand(command);

                // Saving changes:
                session.getTransaction().begin();
                
                session.persist(channel);
                session.persist(command);
                
                session.getTransaction().commit();
                session.close();

                return Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_CMD_SUCCESS_NEW,
                        command.getName()
                );
            }

            // If the command not exists:
            if (optionalCustomCommands.isEmpty()) {
                return Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_CMD_DOESNOTEXISTS,
                        name
                );
            }

            CustomCommand command = optionalCustomCommands.get();

            switch (m.getMessage().getSubCommand()) {
                // "Edit a command response" clause:
                case "edit": {
                    // Setting a new response:
                    command.setMessage(response);

                    // Saving changes:
                    session.getTransaction().begin();

                    session.persist(command);

                    session.getTransaction().commit();
                    session.close();

                    return Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_CMD_SUCCESS_EDIT,
                            command.getName()
                    );
                }
                // "Delete a command" clause:
                case "delete":
                    // Deleting a command and saving changes:
                    session.getTransaction().begin();
                    session.remove(command);
                    session.getTransaction().commit();
                    session.close();

                    return Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_CMD_SUCCESS_DELETE,
                            command.getName()
                    );
                case "rename":
                    String nameToRename = s.get(0);
                    String previousName = command.getName();

                    System.out.println(nameToRename);
                    System.out.println(previousName);

                    command.setName(nameToRename);

                    // Saving changes:
                    session.getTransaction().begin();
                    session.persist(command);
                    session.getTransaction().commit();
                    session.close();

                    return Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.C_CMD_SUCCESS_RENAME,
                            previousName,
                            nameToRename
                    );
                default:
                    break;
            }
        }

        return null;
    }
}
