package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.i18n.LineIds;
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
            return Huinyabot.getInstance().getLocale().literalText(
                    m.getLanguage(),
                    LineIds.NOT_ENOUGH_ARGS
            );
        }

        if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands() == null) {
            Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).setCustomCommands(new HashMap<>());
        }

        final String name = s.get(0);
        s.remove(0);

        if (m.getMessage().getSubCommand() == null) {
            return Huinyabot.getInstance().getLocale().literalText(
                    m.getLanguage(),
                    LineIds.NO_SUBCMD
            );
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
                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_CMD_NOCMDS,
                            ((Huinyabot.getInstance().getTargetLinks().containsKey(name) && Huinyabot.getInstance().getTargetCtrl().getAll().containsKey(Huinyabot.getInstance().getTargetLinks().get(name))) ? name : m.getEvent().getChannel().getName())
                    );
                }

                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.C_CMD_SUCCESS_LIST,
                        ((Huinyabot.getInstance().getTargetLinks().containsKey(name) && Huinyabot.getInstance().getTargetCtrl().getAll().containsKey(Huinyabot.getInstance().getTargetLinks().get(name))) ? name : m.getEvent().getChannel().getName()),
                        String.join(", ", cmds)
                );
            }
            default:
                break;
        }

        if (m.getCurrentPermissions().getId() >= Permissions.BROADCASTER.getId()) {
            switch (m.getMessage().getSubCommand()) {
                case "new": {
                    String response = String.join(" ", s);
                    if (Objects.equals(response, "")) {
                        return Huinyabot.getInstance().getLocale().literalText(
                                m.getLanguage(),
                                LineIds.C_CMD_NOSECONDARG
                        );
                    }

                    CustomCommand cmd = new CustomCommand(
                            name,
                            response,
                            true,
                            new ArrayList<>()
                    );

                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(cmd.getId()) || Huinyabot.getInstance().getLoader().getCommands().containsKey(cmd.getId())) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_ALREADYEXISTS,
                                cmd.getId()
                        );
                    }

                    if (m.getMessage().getOptions().contains("no-mention")) {
                        cmd.setFlag("no-mention");
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().put(cmd.getId(), cmd);
                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_CMD_SUCCESS_NEW,
                            cmd.getId()
                    );
                }
                case "edit": {
                    String response = String.join(" ", s);
                    if (Objects.equals(response, "")) {
                        return Huinyabot.getInstance().getLocale().literalText(
                                m.getLanguage(),
                                LineIds.C_CMD_NOSECONDARG
                        );
                    }

                    CustomCommand cmd = new CustomCommand(
                            name,
                            response,
                            true,
                            new ArrayList<>()
                    );

                    if (!Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(cmd.getId())) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_DOESNOTEXISTS,
                                cmd.getId()
                        );
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(cmd.getId()).setResponse(response);
                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_CMD_SUCCESS_EDIT,
                            cmd.getId()
                    );
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
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_DOESNOTEXISTS,
                                cmd.getId()
                        );
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().remove(cmd.getId());
                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_CMD_SUCCESS_DELETE,
                            cmd.getId()
                    );
                }
                case "rename": {
                    String name2 = s.get(0);
                    if (Objects.equals(name2, "")) {
                        return Huinyabot.getInstance().getLocale().literalText(
                                m.getLanguage(),
                                LineIds.C_CMD_NOSECONDARG
                        );
                    }

                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(name2) || Huinyabot.getInstance().getLoader().getCommands().containsKey(name2)) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_ALREADYEXISTS,
                                name2
                        );
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().put(
                            name2,
                            Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name)
                    );
                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().remove(name);
                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name2).setId(name2);

                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_CMD_SUCCESS_RENAME,
                            name,
                            name2
                    );
                }
                case "copy": {

                    if (s.size() == 0) {
                        return Huinyabot.getInstance().getLocale().literalText(
                                m.getLanguage(),
                                LineIds.C_CMD_NOSECONDARG
                        );
                    }
                    String name2 = s.get(0);

                    if (!Huinyabot.getInstance().getTargetLinks().containsKey(name)) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_NOTFOUND,
                                name
                        );
                    }

                    if (!Huinyabot.getInstance().getTargetCtrl().get(
                            Huinyabot.getInstance().getTargetLinks().get(name)
                    ).getCustomCommands().containsKey(name2)) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_DOESNOTEXISTS,
                                name2
                        );
                    }

                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(name2)) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_ALREADYEXISTS,
                                name2
                        );
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().put(
                            Huinyabot.getInstance().getTargetCtrl().get(Huinyabot.getInstance().getTargetLinks().get(name)).getCustomCommands().get(name2).getId(),
                            Huinyabot.getInstance().getTargetCtrl().get(Huinyabot.getInstance().getTargetLinks().get(name)).getCustomCommands().get(name2)
                    );
                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_CMD_SUCCESS_COPY,
                            name2,
                            name
                    );
                }
                case "toggle": {
                    if (!Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().containsKey(name)) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_DOESNOTEXISTS,
                                name
                        );
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name).setValue(
                            !Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name).getValue()
                    );

                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getCustomCommands().get(name).getValue()) {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_SUCCESS_ENABLE,
                                name
                        );
                    } else {
                        return Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_CMD_SUCCESS_DISABLE,
                                name
                        );
                    }
                }
                default:
                    return null;
            }
        }

        return null;
    }
}
