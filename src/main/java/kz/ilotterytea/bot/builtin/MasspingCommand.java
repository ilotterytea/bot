package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.helix.domain.Chatter;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ping em, Fors! LUL
 * @author ilotterytea
 * @since 1.0
 */
public class MasspingCommand extends Command {
    @Override
    public String getNameId() { return "massping"; }

    @Override
    public int getDelay() { return 60000; }

    @Override
    public Permissions getPermissions() { return Permissions.BROADCASTER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("mp", "масспинг", "мп")); }

    @Override
    public String run(ArgumentsModel m) {
        if (!m.getEvent().getChannelName().isPresent()) return null;

        List<Chatter> chatters;

        try {
            chatters = Huinyabot.getInstance().getClient().getHelix().getChatters(
                    Huinyabot.getInstance().getProperties().getProperty("ACCESS_TOKEN"),
                    m.getEvent().getChannel().getId(),
                    Huinyabot.getInstance().getCredential().getUserId(),
                    null,
                    null
            ).execute().getChatters();
        } catch (Exception e) {
            return Huinyabot.getInstance().getLocale().literalText(
                    m.getLanguage(),
                    LineIds.C_MASSPING_NOTMOD
            );
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
                    .append(m.getMessage().getMessage())
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
                    m.getEvent().getChannel().getName(),
                    msg + m.getMessage().getMessage()
            );
        }

        return null;
    }
}
