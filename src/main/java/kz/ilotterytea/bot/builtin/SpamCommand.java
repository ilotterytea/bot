package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.ParsedMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

/**
 * Spam command.
 * @author ilotterytea
 * @since 1.0
 */
public class SpamCommand implements Command {
    @Override
    public String getNameId() { return "spam"; }

    @Override
    public int getDelay() { return 30000; }

    @Override
    public Permission getPermissions() { return Permission.MOD; }

    @Override
    public List<String> getOptions() { return Collections.singletonList("count"); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return List.of("спам", "насрать", "repeat", "cv", "paste", "cvpaste"); }

    @Override
    public Optional<String> run(IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
    	if (message.getMessage().isEmpty() || message.getMessage().get().split(" ").length == 1) {
    		return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
    				channel.getPreferences().getLanguage(),
    				LineIds.NO_MESSAGE
    		));
    	}
    	
        final int MAX_COUNT = 8;
        ArrayList<String> s = new ArrayList<>(Arrays.asList(message.getMessage().get().split(" ")));
        int count;

        try {
            count = Integer.parseInt(s.get(0));
            s.remove(0);
        } catch (NumberFormatException e) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_SPAM_NOCOUNT
            ));
        }

        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }

        for (int i = 0; i < count; i++) {
            Huinyabot.getInstance().getClient().getChat().sendMessage(
                    channel.getAliasName(),
                    String.format(
                            "%s %s",
                            String.join(" ", s),
                            (message.getUsedOptions().contains("count")) ? i + 1 : ""
                    )
            );
        }

        return Optional.empty();
    }
}
