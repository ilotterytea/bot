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
 * Ping command.
 * @author ilotterytea
 * @since 1.3
 */
public class SetterCommand implements Command {
    @Override
    public String getNameId() { return "set"; }

    @Override
    public int getDelay() { return 5000; }

    @Override
    public Permission getPermissions() { return Permission.BROADCASTER; }

    @Override
    public List<String> getOptions() { return Collections.emptyList(); }

    @Override
    public List<String> getSubcommands() { return List.of("prefix", "locale"); }

    @Override
    public List<String> getAliases() { return Collections.emptyList(); }

    @Override
    public Optional<String> run(IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        if (message.getSubcommandId().isEmpty()) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                    channel.getPreferences().getLanguage(),
                    LineIds.NO_SUBCMD
            ));
        }
        
        if (message.getMessage().isEmpty()) {
        	return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
        			channel.getPreferences().getLanguage(),
        			LineIds.NO_MESSAGE
        	));
        }

        Session session = HibernateUtil.getSessionFactory().openSession();

        switch (message.getSubcommandId().get()) {
	        // "Prefix" clause.
	        case "prefix": {
	            ChannelPreferences preferences = channel.getPreferences();
	            preferences.setPrefix(message.getMessage().get());
	
	            session.getTransaction().begin();
	            session.persist(preferences);
	            session.getTransaction().commit();
	            session.close();
	
	            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
	                    preferences.getLanguage(),
	                    LineIds.C_SET_SUCCESS_PREFIX_SET,
	                    preferences.getPrefix()
	            ));
	        }
	        // "Locale", "language" clause.
	        case "locale":
	            if (!Huinyabot.getInstance().getLocale().getLocaleIds().contains(message.getMessage().get().toLowerCase())) {
	                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
	                        channel.getPreferences().getLanguage(),
	                        LineIds.C_SET_SUCCESS_LOCALE_LIST,
	                        String.join(", ", Huinyabot.getInstance().getLocale().getLocaleIds())
	                ));
	            }
	
	            ChannelPreferences preferences = channel.getPreferences();
	            preferences.setLanguage(message.getMessage().get().toLowerCase());
	
	            session.getTransaction().begin();
	            session.persist(preferences);
	            session.getTransaction().commit();
	            session.close();
	
	            return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
	                    preferences.getLanguage(),
	                    LineIds.C_SET_SUCCESS_LOCALE_SET
	            ));
	        default:
	        	session.close();
	        	return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
	        			channel.getPreferences().getLanguage(),
	        			LineIds.UNKNOWN_SUBCOMMAND
	        	));
        }
    }
}
