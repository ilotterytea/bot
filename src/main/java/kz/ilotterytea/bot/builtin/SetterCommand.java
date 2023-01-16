package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.UserModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * Ping command.
 * @author ilotterytea
 * @since 1.3
 */
public class SetterCommand extends Command {
    @Override
    public String getNameId() { return "set"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Collections.singletonList("self")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(Arrays.asList("prefix", "locale")); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(); }

    @Override
    public String run(ArgumentsModel m) {
        if (m.getMessage().getSubCommand() == null) {
            return Huinyabot.getInstance().getLocale().literalText(
                    m.getLanguage(),
                    LineIds.NO_SUBCMD
            );
        }

        if (Objects.equals(m.getMessage().getMessage(), "") || m.getCurrentPermissions().getId() < Permissions.BROADCASTER.getId()) {
            switch (m.getMessage().getSubCommand()) {
                case "prefix": {
                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_SET_SUCCESS_PREFIX_INFO,
                            (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId())
                                    .getPrefix() != null) ? Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId())
                            .getPrefix() : Huinyabot.getInstance().getProperties().getProperty("PREFIX", SharedConstants.DEFAULT_PREFIX)
                    );
                }
                case "locale": {
                    if (m.getMessage().getOptions().contains("self") && !Objects.equals(m.getMessage().getMessage(), "")) {
                        if (!Huinyabot.getInstance().getLocale().getLocaleIds().contains(m.getMessage().getMessage().toLowerCase())) {
                            return Huinyabot.getInstance().getLocale().formattedText(
                                    m.getLanguage(),
                                    LineIds.C_SET_SUCCESS_LOCALE_LIST,
                                    String.join(", ", Huinyabot.getInstance().getLocale().getLocaleIds())
                            );
                        }

                        UserModel user = Huinyabot.getInstance().getUserCtrl().getOrDefault(m.getSender().getAliasId());

                        user.setLanguage(m.getMessage().getMessage().toLowerCase());

                        Huinyabot.getInstance().getUserCtrl().set(user.getAliasId(), user);

                        return Huinyabot.getInstance().getLocale().literalText(
                                user.getLanguage(),
                                LineIds.C_SET_SUCCESS_LOCALE_SET_USER
                        );
                    }

                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_SET_SUCCESS_LOCALE_INFO,
                            (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId())
                                    .getLanguage() != null) ? Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId())
                                    .getLanguage() : Huinyabot.getInstance().getProperties().getProperty("DEFAULT_LANGUAGE", SharedConstants.DEFAULT_LOCALE_ID)
                    );
                }
                default: return null;
            }
        }

        switch (m.getMessage().getSubCommand()) {
            case "prefix": {
                Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).setPrefix(m.getMessage().getMessage());
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.C_SET_SUCCESS_PREFIX_SET,
                        Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getPrefix()
                );
            }
            case "locale": {
                if (!Huinyabot.getInstance().getLocale().getLocaleIds().contains(m.getMessage().getMessage().toLowerCase())) {
                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_SET_SUCCESS_LOCALE_LIST,
                            String.join(", ", Huinyabot.getInstance().getLocale().getLocaleIds())
                    );
                }

                Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).setLanguage(m.getMessage().getMessage().toLowerCase());

                return Huinyabot.getInstance().getLocale().literalText(
                        Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getLanguage(),
                        LineIds.C_SET_SUCCESS_LOCALE_SET
                );
            }
            default: return null;
        }
    }
}
