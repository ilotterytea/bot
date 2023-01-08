package kz.ilotterytea.bot.builtin;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Emote top command.
 * @author ilotterytea
 * @since 1.1
 */
public class EmoteTopCommand extends Command {
    @Override
    public String getNameId() { return "etop"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("emotetop", "топэмоутов")); }

    @Override
    public String run(ArgumentsModel m) {
        String[] s = m.getMessage().getMessage().split(" ");
        final int MAX_COUNT = 10;
        int count;

        if (s.length == 0) {
            count = MAX_COUNT;
        } else {
            try {
                count = Integer.parseInt(s[1]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                count = MAX_COUNT;
            }
        }

        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }

        TargetModel target = Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId());

        if (!target.getEmotes().containsKey(Provider.SEVENTV)) {
            return "[7TV] The 7TV emotes were not detected.";
        }

        Map<String, Emote> emotes = target.getEmotes().get(Provider.SEVENTV)
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(e -> e.getValue().getCount())))
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (l, r) -> l,
                                LinkedHashMap::new
                        )
                );

        ArrayList<String> keys = new ArrayList<>(emotes.keySet());

        if (keys.size() < count) {
            count = keys.size();
        }

        ArrayList<String> msgs = new ArrayList<>();

        msgs.add("");
        int index = 0;

        for (int i = 0; i < count; i++) {
            if (emotes.keySet().toArray().length < i && emotes.keySet().toArray()[i] == null) continue;

            Emote em = emotes.get(keys.get(i));

            StringBuilder sb = new StringBuilder();

            if (
                    new StringBuilder()
                            .append("[7TV] ")
                            .append(msgs.get(index))
                            .append(i + 1)
                            .append(". ")
                            .append(em.getName())
                            .append((em.isDeleted()) ? "*" : "")
                            .append(" (")
                            .append(em.getCount())
                            .append("); ")
                            .length() < 500
            ) {
                sb.append(msgs.get(index))
                        .append(i + 1)
                        .append(". ")
                        .append(em.getName())
                        .append((em.isDeleted()) ? "*" : "")
                        .append(" (")
                        .append(em.getCount())
                        .append("); ");
            } else {
                msgs.add("");
                index++;
            }

            msgs.remove(index);
            msgs.add(index, sb.toString());
        }

        for (String msg : msgs) {
            Huinyabot.getInstance().getClient().getChat().sendMessage(
                    m.getEvent().getChannel().getName(),
                    "[7TV] " + msg,
                    null,
                    (m.getEvent().getMessageId().isPresent()) ? m.getEvent().getMessageId().get() : null
            );
        }

        return null;
    }
}
