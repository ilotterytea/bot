package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.helix.domain.User;
import com.google.gson.Gson;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
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
            return "Not enough rights to perform this action.";
        }

        List<User> users = Huinyabot.getInstance().getClient().getHelix().getUsers(
                Huinyabot.getInstance().getProperties().getProperty("ACCESS_TOKEN", null),
                null,
                Collections.singletonList(s.get(0))
        ).execute().getUsers();

        if (users.size() == 0) {
            return "User with username "+s.get(0)+" is not found!";
        }

        User user = users.get(0);

        final String id = user.getId();
        final String name = user.getLogin();

        if (Huinyabot.getInstance().getTargetCtrl().get(id) != null) {
            return "The user "+name+" is already in!";
        }

        TargetModel targetModel = Huinyabot.getInstance().getTargetCtrl().getOrDefault(id);
        targetModel.setListeningMode(m.getMessage().getOptions().contains("only-listen"));

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
                    String.format("@%s, FeelsDankMan \uD83D\uDC4B joined your chat room!", name)
            );
        }

        Huinyabot.getInstance().getTargetLinks().put(user.getLogin(), user.getId());

        return String.format(
                "Successfully joined to the %s's chat room!",
                name
        );
    }
}
