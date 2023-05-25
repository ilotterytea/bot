package kz.ilotterytea.bot.builtin.emotes;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.SevenTVAPIClient;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.emoteset.Emote;
import kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.emoteset.EmoteSet;
import kz.ilotterytea.bot.utils.ParsedMessage;
import org.hibernate.Session;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Emote set similarity command.
 * @author ilotterytea
 * @since 1.4
 */
public class EmoteSetSimilarityCommand implements Command {
    @Override
    public String getNameId() { return "esimilarity"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return Collections.singletonList("esim"); }

    @Override
    public Optional<String> run(Session session, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        if (message.getMessage().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_MESSAGE
            ));
        }

        // Setting the origin and target channels:
        String[] s = message.getMessage().get().split(" ");
        String originChannel;
        String targetChannel;

        if (s.length == 1) {
            originChannel = channel.getAliasName();
            targetChannel = s[0];
        } else {
            originChannel = s[0];
            targetChannel = s[1];
        }

        if (originChannel.equals(targetChannel)) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.SAME_TWITCH_USER
            ));
        }

        // Getting Twitch users:
        List<com.github.twitch4j.helix.domain.User> userList = Huinyabot.getInstance().getClient()
                .getHelix()
                .getUsers(
                    Huinyabot.getInstance().getCredential().getAccessToken(),
                    null,
                    List.of(originChannel, targetChannel)
                )
                .execute()
                .getUsers();

        if (userList.size() <= 1) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_TWITCH_USER
            ));
        }

        com.github.twitch4j.helix.domain.User originUser = userList.stream().filter(p -> p.getLogin().equals(originChannel)).findFirst().get();
        com.github.twitch4j.helix.domain.User targetUser = userList.stream().filter(p -> p.getLogin().equals(targetChannel)).findFirst().get();

        // Getting emote sets:
        kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.User originSTVUser = SevenTVAPIClient.getUser(Integer.parseInt(originUser.getId()));
        kz.ilotterytea.bot.thirdpartythings.seventv.api.schemas.User targetSTVUser = SevenTVAPIClient.getUser(Integer.parseInt(targetUser.getId()));

        if (originSTVUser == null || targetSTVUser == null) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_EMOTE_SET
            ));
        }

        EmoteSet originEmoteSet = originSTVUser.getEmoteSet();
        EmoteSet targetEmoteSet = targetSTVUser.getEmoteSet();
        int similarity = 0;

        // Comparing emote set:
        for (Emote emote : originEmoteSet.getEmotes()) {
            if (targetEmoteSet.getEmotes().stream().anyMatch(e -> e.getId().equals(emote.getId()))) {
                similarity += 1;
            }
        }

        if (similarity == 0) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_ESIMILARITY_NOSIMILARITY,
                    Huinyabot.getInstance().getLocale().literalText(
                            channel.getPreferences().getLanguage(),
                            LineIds.STV
                    ),
                    originChannel,
                    targetChannel
            ));
        }

        double percentage = ((float) similarity / (float) targetEmoteSet.getEmotes().size()) * 100.0f;

        return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                channel.getPreferences().getLanguage(),
                LineIds.C_ESIMILARITY_SUCCESS,
                Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.STV
                ),
                originChannel,
                String.valueOf(Math.round(percentage)),
                targetChannel,
                String.valueOf(similarity),
                String.valueOf(targetEmoteSet.getEmotes().size())
        ));
    }
}
