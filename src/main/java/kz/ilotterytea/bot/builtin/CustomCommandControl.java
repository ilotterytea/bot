package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.CustomCommand;
import kz.ilotterytea.bot.models.TargetModel;

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
        ArrayList<String> s = new ArrayList<>(Arrays.asList(m.getMessage().getMessage().split(" ")));
        if (Objects.equals(s.get(0), "")) {
            return "Not enough arguments.";
        }

        if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands() == null) {
            Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).setCustomCommands(new HashMap<>());
        }

        final String name = s.get(0);
        s.remove(0);

        if (m.getMessage().getSubCommand() == null) {
            return "No subcommand.";
        }

        switch (m.getMessage().getSubCommand()) {
            case "list": {
                TargetModel target = (Huinyabot.getInstance().getTargetLinks().containsKey(name) && Huinyabot.getInstance().getTargetCtrl().getAll().containsKey(
                        Huinyabot.getInstance().getTargetLinks().get(name)
                )) ? Huinyabot.getInstance().getTargetCtrl().get(
                        Huinyabot.getInstance().getTargetLinks().get(name)
                ) : Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId());

                if (target == null) {
                    return null;
                }

                List<String> cmds = target.getCustomCommands().values()
                        .stream()
                        .map(CustomCommand::getId)
                        .collect(Collectors.toList());

                if (cmds.size() == 0) {
                    return name + "There are no custom commands in "+((Huinyabot.getInstance().getTargetLinks().containsKey(name) && Huinyabot.getInstance().getTargetCtrl().getAll().containsKey(Huinyabot.getInstance().getTargetLinks().get(name))) ? name : m.getEvent().getChannel().getName())+"'s chat room.";
                }

                return "Available " + ((Huinyabot.getInstance().getTargetLinks().containsKey(name) && Huinyabot.getInstance().getTargetCtrl().getAll().containsKey(Huinyabot.getInstance().getTargetLinks().get(name))) ? name : m.getEvent().getChannel().getName()) + "'s chat room custom commands: " + String.join(", ", cmds);
            }
            default:
                break;
        }

        if (m.getCurrentPermissions().getId() >= Permissions.BROADCASTER.getId()) {
            switch (m.getMessage().getSubCommand()) {
                case "new": {
                    String response = String.join(" ", s);
                    if (Objects.equals(response, "")) {
                        return "The second argument was not found.";
                    }

                    CustomCommand cmd = new CustomCommand(
                            name,
                            response,
                            true,
                            new ArrayList<>()
                    );

                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(cmd.getId()) || Huinyabot.getInstance().getLoader().getCommands().containsKey(cmd.getId())) {
                        return "The command "+cmd.getId()+" already exists!";
                    }

                    if (m.getMessage().getOptions().contains("no-mention")) {
                        cmd.setFlag("no-mention");
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().put(cmd.getId(), cmd);
                    return "A new command was successfully created ("+cmd.getId()+")!";
                }
                case "edit": {
                    String response = String.join(" ", s);
                    if (Objects.equals(response, "")) {
                        return "The second argument was not found.";
                    }

                    CustomCommand cmd = new CustomCommand(
                            name,
                            response,
                            true,
                            new ArrayList<>()
                    );

                    if (!Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(cmd.getId())) {
                        return "The command "+cmd.getId()+" doesn't exists!";
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(cmd.getId()).setResponse(response);
                    return "Successfully edited command response "+cmd.getId()+"!";
                }
                case "delete": {
                    String response = String.join(" ", s);

                    CustomCommand cmd = new CustomCommand(
                            name,
                            response,
                            true,
                            new ArrayList<>()
                    );

                    if (!Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(cmd.getId())) {
                        return "The command "+cmd.getId()+" doesn't exist!";
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().remove(cmd.getId());
                    return "The command "+cmd.getId()+" has been successfully deleted!";
                }
                case "rename": {
                    String name2 = s.get(0);
                    if (Objects.equals(name2, "")) {
                        return "The second argument was not found.";
                    }

                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(name2) || Huinyabot.getInstance().getLoader().getCommands().containsKey(name2)) {
                        return "The command "+name2+" already exists!";
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().put(
                            name2,
                            Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name)
                    );
                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().remove(name);
                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name2).setId(name2);

                    return "Successfully renamed the command from "+name+" to "+name2+"!";
                }
                case "copy": {
                    String name2 = s.get(0);
                    if (Objects.equals(name2, "")) {
                        return "The second argument was not found.";
                    }

                    if (!Huinyabot.getInstance().getTargetLinks().containsKey(name)) {
                        return "The user with the name " + name + " is not found !";
                    }

                    if (!Huinyabot.getInstance().getTargetCtrl().get(
                            Huinyabot.getInstance().getTargetLinks().get(name)
                    ).getCustomCommands().containsKey(name2)) {
                        return "The command "+name2+" doesn't exist in " + name + "'s chat room!";
                    }

                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(name2)) {
                        return "The command "+name2+" already exist in your custom command list!";
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().put(
                            Huinyabot.getInstance().getTargetCtrl().get(Huinyabot.getInstance().getTargetLinks().get(name)).getCustomCommands().get(name2).getId(),
                            Huinyabot.getInstance().getTargetCtrl().get(Huinyabot.getInstance().getTargetLinks().get(name)).getCustomCommands().get(name2)
                    );
                    return "Successfully copied the command "+name2+" from "+name+"'s chat room!";
                }
                case "toggle": {
                    if (!Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(name)) {
                        return "The command " + name + " doesn't exist!";
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name).setValue(
                            !Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name).getValue()
                    );

                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name).getValue()) {
                        return "Successfully enabled the command " + name + "!";
                    } else {
                        return "Successfully disabled the command " + name + "!";
                    }
                }
                default:
                    return null;
            }
        }

        return null;
    }
}
