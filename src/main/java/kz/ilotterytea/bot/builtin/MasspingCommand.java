package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.helix.domain.Chatter;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.ParsedMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ping em, Fors! LUL
 * @author ilotterytea
 * @since 1.0
 */
public class MasspingCommand implements Command {
    @Override
    public String getNameId() { return "massping"; }

    @Override
    public int getDelay() { return 60000; }

    @Override
    public Permission getPermissions() { return Permission.BROADCASTER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return List.of("mp", "масспинг", "мп", "massbing"); }

    @Override
    public Optional<String> run(IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        List<Chatter> chatters;

        try {
            chatters = Huinyabot.getInstance().getClient().getHelix().getChatters(
                    Huinyabot.getInstance().getProperties().getProperty("ACCESS_TOKEN"),
                    channel.getAliasId().toString(),
                    Huinyabot.getInstance().getCredential().getUserId(),
                    null,
                    null
            ).execute().getChatters();
        } catch (Exception e) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_MASSPING_NOTMOD
            ));
        }
        
        String msgToAnnounce;
        
        if (message.getMessage().isEmpty()) {
        	msgToAnnounce = "";
        } else {
        	msgToAnnounce = message.getMessage().get();
        }

        ArrayList<String> msgs = new ArrayList<>();
        msgs.add("");
        
        int index = 0;

        for (Chatter chatter : chatters) {
            StringBuilder sb = new StringBuilder();

            if (new StringBuilder()
                    .append(msgs.get(index))
                    .append("@")
                    .append(chatter.getUserLogin())
                    .append(", ")
                    .append(msgToAnnounce)
                    .length() < 500
            ) {
                sb.append(msgs.get(index)).append("@").append(chatter.getUserLogin()).append(", ");
                msgs.remove(index);
                msgs.add(index, sb.toString());
            } else {
                msgs.add("");
                index++;
            }
        }

        for (String msg : msgs) {
            Huinyabot.getInstance().getClient().getChat().sendMessage(
                    channel.getAliasName(),
                    msg + msgToAnnounce
            );
        }

        return null;
    }
}
