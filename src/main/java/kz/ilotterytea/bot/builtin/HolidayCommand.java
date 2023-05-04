package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.tmi.domain.Chatters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.entities.permissions.UserPermission;
import kz.ilotterytea.bot.entities.users.User;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.HolidayModel;
import kz.ilotterytea.bot.models.serverresponse.Emote;
import kz.ilotterytea.bot.models.serverresponse.ServerPayload;
import kz.ilotterytea.bot.utils.ParsedMessage;
import kz.ilotterytea.bot.utils.StringUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.*;

/**
 * Holiday command.
 * @author ilotterytea
 * @since 1.0
 */
public class HolidayCommand implements Command {
    @Override
    public String getNameId() { return "holiday"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permission getPermissions() { return Permission.USER; }

    @Override
    public List<String> getOptions() { return List.of("тык", "massping", "all", "все", "no-emotes", "без-эмоутов"); }

    @Override
    public List<String> getSubcommands() { return Collections.singletonList("search"); }

    @Override
    public List<String> getAliases() { return List.of("праздник", "hld"); }

    @Override
    public Optional<String> run(IRCMessageEvent event, ParsedMessage message, Channel channel, User user, UserPermission permission) {
        if (message.getSubcommandId().isPresent() && message.getSubcommandId().get().equals("search")) {
            if (message.getMessage().isEmpty()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().literalText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_HOLIDAY_NOSEARCHQUERY
                ));
            }

            Request request = new Request.Builder()
                    .url(String.format(SharedConstants.HOLIDAY_SEARCH_URL, message.getMessage().get()))
                    .build();

            ArrayList<HolidayModel> holidays;

            try {
                Response response = new OkHttpClient().newCall(request).execute();

                if (response.code() == 200) {
                    assert response.body() != null;
                    holidays = new Gson().fromJson(response.body().string(), new TypeToken<ArrayList<HolidayModel>>(){}.getType());
                } else {
                    return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                            channel.getPreferences().getLanguage(),
                            LineIds.HTTP_ERROR,
                            String.valueOf(response.code()),
                            "Holiday"
                    ));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (holidays.isEmpty()) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_HOLIDAY_QUERYNOTFOUND,
                        message.getMessage().get()
                ));
            }

            ArrayList<String> _holidays = new ArrayList<>();

            for (HolidayModel model : holidays) {
                _holidays.add(String.format("%s (%s/%s)", model.getName(), model.getDate().get(1), model.getDate().get(0)));
            }

            ArrayList<String> msgs = new ArrayList<>();
            msgs.add("");
            int index = 0;

            for (String hol : _holidays) {
                if (
                        Huinyabot.getInstance().getLocale().formattedText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_HOLIDAY_QUERYSUCCESS,
                                String.valueOf(holidays.size()),
                                message.getMessage().get(),
                                msgs.get(index) + hol + ", "
                        ).length() > 500
                ) {
                    index++;
                    msgs.add(hol + ", ");
                } else {
                    String c = msgs.get(index);

                    msgs.remove(index);
                    msgs.add(index, c + hol + ", ");
                }
            }

            for (String msg : msgs) {
                Huinyabot.getInstance().getClient().getChat().sendMessage(
                        channel.getAliasName(),
                        Huinyabot.getInstance().getLocale().formattedText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_HOLIDAY_QUERYSUCCESS,
                                String.valueOf(holidays.size()),
                                message.getMessage().get(),
                                msg
                        )
                );
            }

            return null;
        }

        int month;
        int day;

        ArrayList<String> s = new ArrayList<>(Arrays.asList(message.getMessage().get().split(" ")));

        if (s.size() == 0) {
            month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        } else {
            ArrayList<String> date = new ArrayList<>(Arrays.asList(s.get(0).split("/")));

            if (date.size() >= 2) {
                try {
                    month = Integer.parseInt(date.get(1));
                    day = Integer.parseInt(date.get(0));
                } catch (NumberFormatException e) {
                    month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                    day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                }
            } else {
                try {
                    day = Integer.parseInt(date.get(0));
                    month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                } catch (NumberFormatException e) {
                    month = Calendar.getInstance().get(Calendar.MONTH) + 1;
                    day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                }
            }
        }

        Request request = new Request.Builder()
                .url(String.format(SharedConstants.HOLIDAY_URL, month, day))
                .build();

        ArrayList<String> holidays;

        try {
            Response response = new OkHttpClient().newCall(request).execute();

            if (response.code() == 200) {
                assert response.body() != null;
                holidays = new Gson().fromJson(response.body().string(), new TypeToken<ArrayList<String>>(){}.getType());
            } else {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.HTTP_ERROR,
                        String.valueOf(response.code()),
                        "Holiday"
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (holidays.size() == 0) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.C_HOLIDAY_NOHOLIDAYS,
                    StringUtils.pad(day) + "/" + StringUtils.pad(month)
            ));
        }

        String name = holidays.get((int) Math.floor(Math.random() * holidays.size() - 1));

        if (message.getUsedOptions().contains("all") || message.getUsedOptions().contains("все")) {
            return Optional.ofNullable(String.join(", ", holidays));
        }

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request statsRequest = new Request.Builder()
                .get()
                .url(SharedConstants.STATS_URL + "/api/v1/channel/" + channel.getAliasId().toString() + "/emotes")
                .build();

        ArrayList<Emote> emotes;

        try (Response response = client.newCall(statsRequest).execute()) {
            if (response.code() != 200) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.HTTP_ERROR,
                        String.valueOf(response.code()),
                        "Stats API"
                ));
            }

            if (response.body() == null) {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.SOMETHING_WENT_WRONG
                ));
            }

            String body = response.body().string();

            ServerPayload<ArrayList<Emote>> payload = new Gson().fromJson(body, new TypeToken<ServerPayload<ArrayList<Emote>>>(){}.getType());

            if (payload.getData() != null) {
                emotes = payload.getData();
            } else {
                return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                        channel.getPreferences().getLanguage(),
                        LineIds.C_ETOP_NOCHANNELEMOTES,
                        Huinyabot.getInstance().getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.STV
                        ),
                        Huinyabot.getInstance().getLocale().literalText(
                                channel.getPreferences().getLanguage(),
                                LineIds.STV
                        )
                ));
            }
        } catch (IOException e) {
            return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                    channel.getPreferences().getLanguage(),
                    LineIds.SOMETHING_WENT_WRONG
            ));
        }

        if (message.getUsedOptions().contains("massping") || message.getUsedOptions().contains("тык")) {
            ArrayList<String> msgs = new ArrayList<>();
            int index = 0;
            Chatters chatters = Huinyabot.getInstance().getClient().getMessagingInterface().getChatters(channel.getAliasName()).execute();

            msgs.add("");

            for (String uName : chatters.getAllViewers()) {
                StringBuilder sb = new StringBuilder();

                if (!message.getUsedOptions().contains("no-emotes") || !message.getUsedOptions().contains("без-эмоутов")) {
                    String finalUName = uName;
                    Optional<Emote> optionalEmote = emotes.stream().filter(e -> e.getName().equalsIgnoreCase(finalUName)).findFirst();

                    if (optionalEmote.isPresent()) {
                        uName = optionalEmote.get().getName();
                    }
                }

                if (
                        Huinyabot.getInstance().getLocale().formattedText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_HOLIDAY_SUCCESS,
                                msgs.get(index) + uName + " ",
                                StringUtils.pad(day) + "/" + StringUtils.pad(month),
                                String.valueOf(holidays.indexOf(name) + 1),
                                String.valueOf(holidays.size()),
                                name
                        ).length() < 500
                ) {
                    sb.append(msgs.get(index)).append(uName).append(" ");
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
                        Huinyabot.getInstance().getLocale().formattedText(
                                channel.getPreferences().getLanguage(),
                                LineIds.C_HOLIDAY_SUCCESS,
                                msg,
                                StringUtils.pad(day) + "/" + StringUtils.pad(month),
                                String.valueOf(holidays.indexOf(name) + 1),
                                String.valueOf(holidays.size()),
                                name
                        )
                );
            }

            return null;
        }

        return Optional.ofNullable(Huinyabot.getInstance().getLocale().formattedText(
                channel.getPreferences().getLanguage(),
                LineIds.C_HOLIDAY_SUCCESS,
                "",
                StringUtils.pad(day) + "/" + StringUtils.pad(month),
                String.valueOf(holidays.indexOf(name) + 1),
                String.valueOf(holidays.size()),
                name
        ));
    }
}
