package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.tmi.domain.Chatters;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;

import java.util.ArrayList;
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
    public ArrayList<String> getAliases() { return new ArrayList<>(List.of(new String[]{"mp", "масспинг", "мп"})); }

    @Override
    public String run(ArgumentsModel m) {
        if (m.getEvent().getChannelName().isEmpty()) return null;

        Chatters chatters = Huinyabot.getInstance().getClient().getMessagingInterface().getChatters(m.getEvent().getChannelName().get()).execute();

        ArrayList<String> msgs = new ArrayList<>();
        msgs.add("");
        int index = 0;

        for (String name : chatters.getAllViewers()) {
            StringBuilder sb = new StringBuilder();

            if (new StringBuilder()
                    .append(msgs.get(index))
                    .append("@")
                    .append(name)
                    .append(", ")
                    .append(m.getMessage().getMessage())
                    .length() < 500
            ) {
                sb.append(msgs.get(index)).append("@").append(name).append(", ");
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
