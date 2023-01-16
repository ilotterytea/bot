package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.helix.domain.User;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.notify.NotifyListener;
import kz.ilotterytea.bot.models.notify.NotifySubscriber;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Notify command.
 * @author ilotterytea
 * @since 1.3
 */
public class NotifyMeCommand extends Command {
    @Override
    public String getNameId() { return "notify"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Arrays.asList("massping", "clear", "no-massping", "no-sub", "announce")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(Arrays.asList("subscribe", "unsubscribe", "list", "on", "off", "message", "flag", "unflag", "subs", "subscriptions", "sub", "unsub", "icon")); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(); }

    @Override
    public String run(ArgumentsModel m) {
        final int MAX_LISTEN_COUNT = 15;
        final ArrayList<String> EVENTS = new ArrayList<>(Arrays.asList("live", "offline", "title", "game"));

        TargetModel target = Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId());

        if (m.getMessage().getSubCommand() == null) {
            return null;
        }

        switch (m.getMessage().getSubCommand()) {
            case "subs":
            case "subscriptions": {
                if (target.getListeners().keySet().size() == 0) {
                    return Huinyabot.getInstance().getLocale().literalText(
                            m.getLanguage(),
                            LineIds.C_NOTIFY_NOLISTENINGCHANNELS
                    );
                }
                ArrayList<String> msgs = new ArrayList<>();
                int index = 0;

                msgs.add("");

                List<User> users = new ArrayList<>(Huinyabot.getInstance().getClient().getHelix().getUsers(
                        Huinyabot.getInstance().getProperties().getProperty("ACCESS_TOKEN", null),
                        new ArrayList<>(target.getListeners().keySet()),
                        null
                ).execute().getUsers());

                for (User user : users) {
                    NotifyListener listener = target.getListeners().get(user.getId());
                    NotifySubscriber subscriber = listener.getSubscribers()
                            .stream().filter(l -> Objects.equals(l.getAliasId(), m.getSender().getAliasId()))
                            .findFirst().orElse(null);

                    if (subscriber == null) {
                        continue;
                    }

                    StringBuilder sb = new StringBuilder();

                    if (
                            Huinyabot.getInstance().getLocale().formattedText(
                                    m.getLanguage(),
                                    LineIds.C_NOTIFY_SUCCESS_SUBS,
                                    msgs.get(index) + user.getLogin() + " (" + String.join(
                                            ",", subscriber.getSubscribedEvents()
                                    ) + "); "
                            ).length() < 500
                    ) {
                        sb.append(msgs.get(index)).append(user.getLogin()).append(" (").append(String.join(",", subscriber.getSubscribedEvents())).append("); ");
                        msgs.remove(index);
                        msgs.add(index, sb.toString());
                    } else {
                        msgs.add("");
                        index++;
                    }
                }

                if (msgs.get(0).equals("")) {
                    return Huinyabot.getInstance().getLocale().literalText(
                            m.getLanguage(),
                            LineIds.C_NOTIFY_SUCCESS_SUBSNOONE
                    );
                }

                for (String msg : msgs) {
                    Huinyabot.getInstance().getClient().getChat().sendMessage(
                            m.getEvent().getChannel().getName(),
                            Huinyabot.getInstance().getLocale().formattedText(
                                    m.getLanguage(),
                                    LineIds.C_NOTIFY_SUCCESS_SUBS,
                                    msg
                            ),
                            null,
                            (m.getEvent().getMessageId().isPresent()) ? m.getEvent().getMessageId().get() : null
                    );
                }

                return null;
            }
            case "list": {
                if (target.getListeners().keySet().size() == 0) {
                    return Huinyabot.getInstance().getLocale().literalText(
                            m.getLanguage(),
                            LineIds.C_NOTIFY_NOLISTENINGCHANNELS
                    );
                }
                ArrayList<String> msgs = new ArrayList<>();
                int index = 0;

                msgs.add("");

                List<User> users = new ArrayList<>(Huinyabot.getInstance().getClient().getHelix().getUsers(
                        Huinyabot.getInstance().getProperties().getProperty("ACCESS_TOKEN", null),
                        new ArrayList<>(target.getListeners().keySet()),
                        null
                ).execute().getUsers());

                for (User user : users) {
                    NotifyListener listener = target.getListeners().get(user.getId());
                    StringBuilder sb = new StringBuilder();

                    if (
                            Huinyabot.getInstance().getLocale().formattedText(
                                    m.getLanguage(),
                                    LineIds.C_NOTIFY_SUCCESS_LIST,
                                    msgs.get(index) + user.getLogin() + " (" + String.join(
                                            ",", listener.getEvents()
                                    ) + "); "
                            ).length() < 500
                    ) {
                        sb.append(msgs.get(index)).append(user.getLogin()).append(" (").append(String.join(",", listener.getEvents())).append("); ");
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
                            Huinyabot.getInstance().getLocale().formattedText(
                                    m.getLanguage(),
                                    LineIds.C_NOTIFY_SUCCESS_LIST,
                                    msg
                            ),
                            null,
                            (m.getEvent().getMessageId().isPresent()) ? m.getEvent().getMessageId().get() : null
                    );
                }

                return null;
            }
            default: {
                break;
            }
        }

        ArrayList<String> s = new ArrayList<>(Arrays.asList(m.getMessage().getMessage().split(" ")));
        if (s.size() == 0 || Objects.equals(s.get(0), "")) {
            return Huinyabot.getInstance().getLocale().literalText(
                    m.getLanguage(),
                    LineIds.NOT_ENOUGH_ARGS
            );
        }
        ArrayList<String> subInfo = new ArrayList<>(Arrays.asList(s.get(0).split(":")));
        if (subInfo.size() == 0) {
            return Huinyabot.getInstance().getLocale().literalText(
                    m.getLanguage(),
                    LineIds.NOT_ENOUGH_ARGS
            );
        }
        s.remove(0);

        final String STREAMER_NAME = subInfo.get(0).toLowerCase();
        final String SUB_EVENT = (subInfo.size() > 1) ? subInfo.get(1).toLowerCase() : null;

        List<User> users = Huinyabot.getInstance().getClient().getHelix().getUsers(
                Huinyabot.getInstance().getProperties().getProperty("ACCESS_TOKEN", null),
                null,
                Collections.singletonList(STREAMER_NAME)
        ).execute().getUsers();

        if (users.size() == 0) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.C_NOTIFY_USERNOTFOUND
            );
        }

        final User USER = users.get(0);

        if (m.getCurrentPermissions().getId() >= Permissions.BROADCASTER.getId()) {
            switch (m.getMessage().getSubCommand()) {
                case "on": {
                    if (!target.getListeners().containsKey(USER.getId()) && target.getListeners().keySet().size() + 1 > MAX_LISTEN_COUNT) {
                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_EXCEEDEDLIMIT,
                                        String.valueOf(target.getListeners().keySet().size()),
                                        String.valueOf(MAX_LISTEN_COUNT)
                                );
                    }

                    NotifyListener listener = target.getListeners().getOrDefault(
                            USER.getId(),
                            new NotifyListener(
                                new HashMap<>(),
                                new HashMap<>(),
                                new ArrayList<>(),
                                new ArrayList<>(),
                                new HashMap<>()
                            )
                    );

                    if (SUB_EVENT != null) {
                        for (String event : SUB_EVENT.split(",")) {
                            event = event.toLowerCase();
                            if (!listener.getEvents().contains(event) && EVENTS.contains(event)) {
                                listener.getEvents().add(event);
                            }
                        }
                    } else {
                        for (String event : EVENTS) {
                            if (!listener.getEvents().contains(event)) {
                                listener.getEvents().add(event);
                            }
                        }
                    }

                    boolean someoneElseListening = false;

                    for (TargetModel target2 : Huinyabot.getInstance().getTargetCtrl().getAll().values()) {
                        if (target2.getListeners().containsKey(USER.getId())) {
                            someoneElseListening = true;
                            break;
                        }
                    }

                    if (!someoneElseListening) {
                        Huinyabot.getInstance().getClient().getClientHelper().enableStreamEventListener(USER.getLogin());
                    }

                    Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                    return Huinyabot.getInstance().getLocale()
                            .formattedText(
                                    m.getLanguage(),
                                    LineIds.C_NOTIFY_SUCCESS_ON,
                                    listener.getEvents().stream().map(ev->"\""+ev+"\"").collect(Collectors.joining(", ")),
                                    USER.getLogin()
                            );
                }
                case "off": {
                    if (!target.getListeners().containsKey(USER.getId())) {
                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_DOESNOTLISTENING,
                                        USER.getLogin()
                                );
                    }

                    NotifyListener listener = target.getListeners().get(USER.getId());

                    // Setting subscribe events
                    if (SUB_EVENT != null) {
                        for (String event : SUB_EVENT.split(",")) {
                            event = event.toLowerCase();
                            if (listener.getEvents().contains(event) && EVENTS.contains(event)) {
                                listener.getEvents().remove(event);
                            }
                        }

                        if (listener.getEvents().size() == 0) {
                            Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                            listener = null;
                        } else {
                            Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                            Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);
                        }
                    } else {
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        listener = null;
                    }

                    boolean someoneElseListening = false;

                    for (TargetModel target2 : Huinyabot.getInstance().getTargetCtrl().getAll().values()) {
                        if (target2.getListeners().containsKey(USER.getId())) {
                            someoneElseListening = true;
                            break;
                        }
                    }

                    if (!someoneElseListening) {
                        Huinyabot.getInstance().getClient().getClientHelper().disableStreamEventListenerForId(USER.getId());
                    }

                    if (listener == null) {
                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_SUCCESS_OFFFULL,
                                        USER.getLogin()
                                );
                    } else {
                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_SUCCESS_OFF,
                                        listener.getEvents().stream().map(ev -> "\"" + ev + "\"").collect(Collectors.joining(", ")),
                                        USER.getLogin()
                                );
                    }
                }
                case "message": {
                    if (!m.getMessage().getOptions().contains("clear") && s.size() == 0) {
                        return Huinyabot.getInstance().getLocale()
                                .literalText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_NOMSG
                                );
                    }

                    if (!target.getListeners().containsKey(USER.getId())) {
                         return Huinyabot.getInstance().getLocale()
                                 .formattedText(
                                         m.getLanguage(),
                                         LineIds.C_NOTIFY_DOESNOTLISTENING,
                                         USER.getLogin()
                                 );
                    }

                    NotifyListener listener = target.getListeners().get(USER.getId());
                    ArrayList<String> args = new ArrayList<>();

                    if (SUB_EVENT != null) {
                        for (String event : SUB_EVENT.split(",")) {
                            event = event.toLowerCase();
                            if (listener.getEvents().contains(event) && EVENTS.contains(event)) {
                                args.add(event);
                                if (m.getMessage().getOptions().contains("clear")) {
                                    listener.getMessages().remove(event);
                                    continue;
                                }
                                listener.getMessages().put(event, String.join(" ", s));
                            }
                        }

                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                        if (m.getMessage().getOptions().contains("clear")) {
                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_COMMENT_REMOVED,
                                            args.stream().map(a -> "\"" + a + "\"").collect(Collectors.joining(", ")),
                                            USER.getLogin()
                                    );
                        } else {
                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_COMMENT_UPDATED,
                                            args.stream().map(a->"\""+a+"\"").collect(Collectors.joining(", ")),
                                            USER.getLogin()
                                    );
                        }

                    } else {
                        for (String event : EVENTS) {
                            if (listener.getEvents().contains(event)) {
                                if (m.getMessage().getOptions().contains("clear")) {
                                    listener.getMessages().remove(event);
                                    continue;
                                }
                                listener.getMessages().put(event, String.join(" ", s));
                            }
                        }

                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                        if (m.getMessage().getOptions().contains("clear")) {
                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_COMMENT_REMOVEDALL,
                                            USER.getLogin()
                                    );
                        } else {
                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_COMMENT_UPDATEDALL,
                                            USER.getLogin()
                                    );
                        }
                    }
                }
                case "icon": {
                    if (!m.getMessage().getOptions().contains("clear") && s.size() == 0) {
                        return Huinyabot.getInstance().getLocale()
                                .literalText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_NOMSG
                                );
                    }

                    if (!target.getListeners().containsKey(USER.getId())) {
                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_DOESNOTLISTENING,
                                        USER.getLogin()
                                );
                    }

                    NotifyListener listener = target.getListeners().get(USER.getId());
                    ArrayList<String> args = new ArrayList<>();

                    if (SUB_EVENT != null) {
                        for (String event : SUB_EVENT.split(",")) {
                            event = event.toLowerCase();
                            if (listener.getEvents().contains(event) && EVENTS.contains(event)) {
                                args.add(event);
                                if (m.getMessage().getOptions().contains("clear")) {
                                    listener.getIcons().remove(event);
                                    continue;
                                }
                                listener.getIcons().put(event, String.join(" ", s));
                            }
                        }

                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                        if (m.getMessage().getOptions().contains("clear")) {
                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_ICON_REMOVED,
                                            args.stream().map(a -> "\"" + a + "\"").collect(Collectors.joining(", ")),
                                            USER.getLogin()
                                    );
                        } else {
                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_ICON_UPDATED,
                                            args.stream().map(a->"\""+a+"\"").collect(Collectors.joining(", ")),
                                            USER.getLogin()
                                    );
                        }

                    } else {
                        for (String event : EVENTS) {
                            if (listener.getEvents().contains(event)) {
                                if (m.getMessage().getOptions().contains("clear")) {
                                    listener.getIcons().remove(event);
                                    continue;
                                }
                                listener.getIcons().put(event, String.join(" ", s));
                            }
                        }

                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                        if (m.getMessage().getOptions().contains("clear")) {
                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_ICON_REMOVEDALL,
                                            USER.getLogin()
                                    );
                        } else {
                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_ICON_UPDATEDALL,
                                            USER.getLogin()
                                    );
                        }
                    }
                }
                case "flag": {
                    if (!target.getListeners().containsKey(USER.getId())) {
                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_DOESNOTLISTENING,
                                        USER.getLogin()
                                );
                    }

                    if (m.getMessage().getOptions().size() == 0) {
                        return Huinyabot.getInstance().getLocale()
                                .literalText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_NOFLAG
                                );
                    }

                    NotifyListener listener = target.getListeners().get(USER.getId());
                    ArrayList<String> args = new ArrayList<>();

                    if (SUB_EVENT != null) {
                        for (String event : SUB_EVENT.split(",")) {
                            event = event.toLowerCase();
                            if (listener.getEvents().contains(event) && EVENTS.contains(event)) {
                                args.add(event);
                                if (!listener.getFlags().containsKey(event)) {
                                    listener.getFlags().put(event, m.getMessage().getOptions());
                                    continue;
                                }

                                for (String o : m.getMessage().getOptions()) {
                                    if (!listener.getFlags().get(event).contains(o)) {
                                        listener.getFlags().get(event).add(o);
                                    }
                                }
                            }
                        }

                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_SUCCESS_FLAG,
                                        m.getMessage().getOptions().stream().map(a->"\""+a+"\"").collect(Collectors.joining(", ")),
                                        args.stream().map(a->"\""+a+"\"").collect(Collectors.joining(", ")),
                                        USER.getLogin()
                                );
                    } else {
                        for (String event : EVENTS) {
                            if (listener.getEvents().contains(event)) {
                                if (!listener.getFlags().containsKey(event)) {
                                    listener.getFlags().put(event, m.getMessage().getOptions());
                                    continue;
                                }

                                for (String o : m.getMessage().getOptions()) {
                                    if (!listener.getFlags().get(event).contains(o)) {
                                        listener.getFlags().get(event).add(o);
                                    }
                                }
                            }
                        }

                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_SUCCESS_FLAGALL,
                                        m.getMessage().getOptions().stream().map(a->"\""+a+"\"").collect(Collectors.joining(", ")),
                                        USER.getLogin()
                                );
                    }
                }
                case "unflag": {
                    if (!target.getListeners().containsKey(USER.getId())) {
                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_DOESNOTLISTENING,
                                        USER.getLogin()
                                );
                    }

                    NotifyListener listener = target.getListeners().get(USER.getId());
                    ArrayList<String> args = new ArrayList<>();

                    if (SUB_EVENT != null) {
                        for (String event : SUB_EVENT.split(",")) {
                            event = event.toLowerCase();
                            if (listener.getEvents().contains(event) && EVENTS.contains(event)) {
                                if (listener.getFlags().containsKey(event)) {
                                    args.add(event);
                                    if (m.getMessage().getOptions().size() == 0){
                                        listener.getFlags().remove(event);
                                        continue;
                                    }
                                    for (String o : m.getMessage().getOptions()) {
                                        listener.getFlags().get(event).remove(o);
                                    }
                                }
                            }
                        }

                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_SUCCESS_UNFLAG,
                                        m.getMessage().getOptions().stream().map(a->"\""+a+"\"").collect(Collectors.joining(", ")),
                                        args.stream().map(a->"\""+a+"\"").collect(Collectors.joining(", ")),
                                        USER.getLogin()
                                );
                    } else {
                        for (String event : EVENTS) {
                            if (listener.getEvents().contains(event)) {
                                if (listener.getFlags().containsKey(event)) {
                                    if (m.getMessage().getOptions().size() == 0){
                                        listener.getFlags().remove(event);
                                        continue;
                                    }
                                    for (String o : m.getMessage().getOptions()) {
                                        listener.getFlags().get(event).remove(o);
                                    }
                                }
                            }
                        }

                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().remove(USER.getId());
                        Huinyabot.getInstance().getTargetCtrl().get(target.getAliasId()).getListeners().put(USER.getId(), listener);

                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_SUCCESS_UNFLAGALL,
                                        m.getMessage().getOptions().stream().map(a->"\""+a+"\"").collect(Collectors.joining(", ")),
                                        USER.getLogin()
                                );
                    }
                }
                default: {
                    break;
                }
            }
        }

        if (!target.getListeners().containsKey(USER.getId())) {
            return Huinyabot.getInstance().getLocale()
                    .formattedText(
                            m.getLanguage(),
                            LineIds.C_NOTIFY_DOESNOTLISTENING,
                            USER.getLogin()
                    );
        }

        NotifyListener listener = target.getListeners().getOrDefault(USER.getId(), null);

        if (listener != null) {
            switch (m.getMessage().getSubCommand()) {
                case "sub":
                case "subscribe": {
                    NotifySubscriber subscriber = listener.getSubscribers()
                            .stream()
                            .filter(sub -> Objects.equals(sub.getAliasId(), m.getSender().getAliasId()))
                            .findFirst().orElse(new NotifySubscriber(new ArrayList<>(), m.getSender().getAliasId()));

                    ArrayList<String> subs = new ArrayList<>();

                    if (SUB_EVENT != null) {
                        for (String event : SUB_EVENT.split(",")) {
                            event = event.toLowerCase();
                            if (EVENTS.contains(event) && !subscriber.getSubscribedEvents().contains(event)) {
                                subscriber.subscribeToEvent(event);
                                subs.add(event);
                            }
                        }
                    } else {
                        for (String event : EVENTS) {
                            if (!subscriber.getSubscribedEvents().contains(event)) {
                                subscriber.subscribeToEvent(event);
                                subs.add(event);
                            }
                        }
                    }

                    if (subs.size() == 0) {
                        return Huinyabot.getInstance().getLocale()
                                .literalText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_ALREADYSUB
                                );
                    }

                    int index = Huinyabot.getInstance().getTargetCtrl()
                            .get(target.getAliasId())
                            .getListeners()
                            .get(USER.getId())
                            .getSubscribers()
                            .indexOf(
                                    Huinyabot.getInstance().getTargetCtrl()
                                            .get(target.getAliasId())
                                            .getListeners()
                                            .get(USER.getId())
                                            .getSubscribers()
                                            .stream().filter(sub -> Objects.equals(sub.getAliasId(), m.getSender().getAliasId()))
                                            .findFirst().orElse(null)
                            );

                    if (index == -1) {
                        Huinyabot.getInstance().getTargetCtrl()
                                .get(target.getAliasId())
                                .getListeners()
                                .get(USER.getId())
                                .getSubscribers()
                                .add(subscriber);
                    } else {
                        Huinyabot.getInstance().getTargetCtrl()
                                .get(target.getAliasId())
                                .getListeners()
                                .get(USER.getId())
                                .getSubscribers()
                                .remove(index);

                        Huinyabot.getInstance().getTargetCtrl()
                                .get(target.getAliasId())
                                .getListeners()
                                .get(USER.getId())
                                .getSubscribers()
                                .add(index, subscriber);
                    }

                    return Huinyabot.getInstance().getLocale()
                            .formattedText(
                                    m.getLanguage(),
                                    LineIds.C_NOTIFY_SUCCESS_SUB,
                                    subs.stream().map(sub->"\""+sub+"\"").collect(Collectors.joining(", ")),
                                    USER.getLogin()
                            );
                }
                case "unsub":
                case "unsubscribe": {
                    NotifySubscriber subscriber = listener.getSubscribers()
                            .stream()
                            .filter(sub -> Objects.equals(sub.getAliasId(), m.getSender().getAliasId()))
                            .findFirst().orElse(null);

                    if (subscriber == null) {
                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_NOTSUB,
                                        USER.getLogin()
                                );
                    }

                    int index = listener.getSubscribers().indexOf(subscriber);

                    ArrayList<String> subs = new ArrayList<>();

                    if (SUB_EVENT != null) {
                        for (String event : SUB_EVENT.split(",")) {
                            event = event.toLowerCase();
                            if (EVENTS.contains(event) && subscriber.getSubscribedEvents().contains(event)) {
                                subscriber.unsubscribeFromEvent(event);
                                subs.add(event);
                            }
                        }

                        if (subscriber.getSubscribedEvents().size() == 0) {
                            Huinyabot.getInstance().getTargetCtrl()
                                    .get(target.getAliasId())
                                    .getListeners()
                                    .get(USER.getId())
                                    .getSubscribers()
                                    .remove(index);

                            return Huinyabot.getInstance().getLocale()
                                    .formattedText(
                                            m.getLanguage(),
                                            LineIds.C_NOTIFY_SUCCESS_UNSUBFULL,
                                            USER.getLogin()
                                    );
                        }
                    } else {
                        Huinyabot.getInstance().getTargetCtrl()
                                .get(target.getAliasId())
                                .getListeners()
                                .get(USER.getId())
                                .getSubscribers()
                                .remove(index);

                        return Huinyabot.getInstance().getLocale()
                                .formattedText(
                                        m.getLanguage(),
                                        LineIds.C_NOTIFY_SUCCESS_UNSUBFULL,
                                        USER.getLogin()
                                );
                    }

                    return Huinyabot.getInstance().getLocale()
                            .formattedText(
                                    m.getLanguage(),
                                    LineIds.C_NOTIFY_SUCCESS_UNSUB,
                                    subs.stream().map(sub->"\""+sub+"\"").collect(Collectors.joining(", ")),
                                    USER.getLogin()
                            );
                }
                default: {
                    break;
                }
            }
        }

        return null;
    }
}
