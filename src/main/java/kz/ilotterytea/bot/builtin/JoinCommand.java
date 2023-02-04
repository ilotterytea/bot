package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.helix.domain.User;
import com.google.gson.Gson;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.SevenTVEmoteLoader;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.EmoteAPIData;
import kz.ilotterytea.bot.thirdpartythings.seventv.v1.models.Message;

import java.util.*;

/**
 * Join command.
 * @author ilotterytea
 * @since 1.1
 */
public class JoinCommand extends Command {
    @Override
    public String getNameId() { return "join"; }

    @Override
    public int getDelay() { return 120000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Arrays.asList("silent", "тихо", "only-listen")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("зайти")); }

    @Override
    public String run(ArgumentsModel m) {
        ArrayList<String> s = new ArrayList<>(Arrays.asList(m.getMessage().getMessage().split(" ")));

        if (Objects.equals(s.get(0), "")) {
            s.add(0, m.getEvent().getUserName());
        } else if (m.getCurrentPermissions().getId() < Permissions.SUPAUSER.getId()) {
            return Huinyabot.getInstance().getLocale().literalText(
                    m.getLanguage(),
                    LineIds.NO_RIGHTS
            );
        }

        List<User> users = Huinyabot.getInstance().getClient().getHelix().getUsers(
                Huinyabot.getInstance().getProperties().getProperty("ACCESS_TOKEN", null),
                null,
                Collections.singletonList(s.get(0))
        ).execute().getUsers();

        if (users.size() == 0) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.C_JOIN_NOTFOUND,
                    s.get(0)
            );
        }

        User user = users.get(0);

        final String id = user.getId();
        final String name = user.getLogin();

        if (Huinyabot.getInstance().getTargetCtrl().get(id) != null) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.C_JOIN_ALREADYIN,
                    name
            );
        }

        TargetModel targetModel = Huinyabot.getInstance().getTargetCtrl().getOrDefault(id);

        if (m.getMessage().getOptions().contains("only-listen") && !targetModel.getFlags().contains("listen-only")) {
            targetModel.getFlags().add("listen-only");
        }

        Huinyabot.getInstance().getTargetCtrl().set(id, targetModel);

        ArrayList<EmoteAPIData> channelEmotes = new SevenTVEmoteLoader().getChannelEmotes(name);

        if (channelEmotes != null) {
            if (!Huinyabot.getInstance().getTargetCtrl().get(id).getEmotes().containsKey(Provider.SEVENTV)) {
                Huinyabot.getInstance().getTargetCtrl().get(id).getEmotes().put(Provider.SEVENTV, new HashMap<>());
            }

            for (EmoteAPIData emote : channelEmotes) {
                if (!Huinyabot.getInstance().getTargetCtrl().get(id).getEmotes().get(Provider.SEVENTV).containsKey(emote.getId())) {
                    Huinyabot.getInstance().getTargetCtrl().get(id).getEmotes().get(Provider.SEVENTV).put(
                            emote.getId(),
                            new Emote(
                                    emote.getId(),
                                    Provider.SEVENTV,
                                    emote.getName(),
                                    0,
                                    false,
                                    false
                            )
                    );
                }
            }

            Huinyabot.getInstance().getSevenTVWSClient().send(
                    new Gson().toJson(new Message("join", name))
            );
        }

        ArrayList<EmoteAPIData> globalEmotes = new SevenTVEmoteLoader().getGlobalEmotes();

        if (globalEmotes != null) {
            if (!Huinyabot.getInstance().getTargetCtrl().get(id).getEmotes().containsKey(Provider.SEVENTV)) {
                Huinyabot.getInstance().getTargetCtrl().get(id).getEmotes().put(Provider.SEVENTV, new HashMap<>());
            }

            for (EmoteAPIData emote : globalEmotes) {
                if (!Huinyabot.getInstance().getTargetCtrl().get(id).getEmotes().get(Provider.SEVENTV).containsKey(emote.getId())) {
                    Huinyabot.getInstance().getTargetCtrl().get(id).getEmotes().get(Provider.SEVENTV).put(
                            emote.getId(),
                            new Emote(
                                    emote.getId(),
                                    Provider.SEVENTV,
                                    emote.getName(),
                                    0,
                                    true,
                                    false
                            )
                    );
                }
            }
        }

        Huinyabot.getInstance().getClient().getChat().joinChannel(name);
        if (!m.getMessage().getOptions().contains("silent") && !m.getMessage().getOptions().contains("тихо") && !m.getMessage().getOptions().contains("only-listen")) {
            Huinyabot.getInstance().getClient().getChat().sendMessage(
                    name,
                    Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.C_JOIN_SUCCESSCHAT,
                            name
                    )
            );
        }

        Huinyabot.getInstance().getTargetLinks().put(user.getLogin(), user.getId());

        return Huinyabot.getInstance().getLocale().formattedText(
                m.getLanguage(),
                LineIds.C_JOIN_SUCCESS,
                name
        );
    }
}
