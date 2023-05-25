package kz.ilotterytea.bot.builtin.channel;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.channels.ChannelPreferences;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.utils.HibernateUtil;
import kz.ilotterytea.bot.utils.ParsedMessage;

import org.hibernate.Session;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.*;

/**
 * Join command.
 * @author ilotterytea
 * @since 1.1
 */
public class JoinCommand implements Command {
    @Override
    public String getNameId() { return "join"; }

    @Override
    public int getDelay() { return 120000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return List.of("silent", "тихо", "only-listen"); }

    @Override
    public List<String> getSubcommands() { return Collections.emptyList(); }

    @Override
    public List<String> getAliases() { return Collections.singletonList("зайти"); }

    @Override
    public Optional<String> run(Session session, IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        // Getting the sender's local channel info if it exists:
        List<Channel> userChannels = session.createQuery("from Channel where aliasId = :aliasId", Channel.class)
                .setParameter("aliasId", user.getAliasId())
                .getResultList();

        Channel userChannel;

        // Creating a new channel if it does not exist:
        if (userChannels.isEmpty()) {
        	userChannel = new Channel(user.getAliasId(), user.getAliasName());
            ChannelPreferences preferences = new ChannelPreferences(userChannel);
            userChannel.setPreferences(preferences);

            session.persist(userChannel);
            session.persist(preferences);
        } else {
        	userChannel = userChannels.get(0);

            // If the channel has already been opt-outed, opt-in:
            if (userChannel.getOptOutTimestamp() != null) {
            	userChannel.setOptOutTimestamp(null);
            	userChannel.setAliasName(user.getAliasName());
            	
            	session.persist(userChannel);
            } else {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                		channel.getPreferences().getLanguage(),
                        LineIds.C_JOIN_ALREADYIN,
                        channel.getAliasName()
                ));
            }
        }

        Huinyabot.getInstance().getClient().getChat().joinChannel(userChannel.getAliasName());

        return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                channel.getPreferences().getLanguage(),
                LineIds.C_JOIN_SUCCESS,
                userChannel.getAliasName()
        ));
    }
}
