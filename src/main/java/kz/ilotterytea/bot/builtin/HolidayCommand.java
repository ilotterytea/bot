package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.tmi.domain.Chatters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.HolidayModel;
import kz.ilotterytea.bot.models.serverresponse.Emote;
import kz.ilotterytea.bot.models.serverresponse.ServerPayload;
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
public class HolidayCommand extends Command {
    @Override
    public String getNameId() { return "holiday"; }

    @Override
    public int getDelay() { return 10000; }

    @Override
    public Permissions getPermissions() { return Permissions.USER; }

    @Override
    public ArrayList<String> getOptions() { return new ArrayList<>(Arrays.asList("тык", "massping", "all", "все", "no-emotes", "без-эмоутов")); }

    @Override
    public ArrayList<String> getSubcommands() { return new ArrayList<>(Collections.singletonList("search")); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("праздник", "hld")); }

    @Override
    public String run(ArgumentsModel m) {
        if (m.getMessage().getSubCommand() != null && m.getMessage().getSubCommand().equals("search")) {
            if (m.getMessage().getMessage() == null || m.getMessage().getMessage().equals("")) {
                return Huinyabot.getInstance().getLocale().literalText(
                        m.getLanguage(),
                        LineIds.C_HOLIDAY_NOSEARCHQUERY
                );
            }

            Request request = new Request.Builder()
                    .url(String.format(SharedConstants.HOLIDAY_SEARCH_URL, m.getMessage().getMessage()))
                    .build();

            ArrayList<HolidayModel> holidays;

            try {
                Response response = new OkHttpClient().newCall(request).execute();

                if (response.code() == 200) {
                    assert response.body() != null;
                    holidays = new Gson().fromJson(response.body().string(), new TypeToken<ArrayList<HolidayModel>>(){}.getType());
                } else {
                    return Huinyabot.getInstance().getLocale().formattedText(
                            m.getLanguage(),
                            LineIds.HTTP_ERROR,
                            String.valueOf(response.code()),
                            "Holiday"
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (holidays.isEmpty()) {
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.C_HOLIDAY_QUERYNOTFOUND,
                        m.getMessage().getMessage()
                );
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
                                m.getLanguage(),
                                LineIds.C_HOLIDAY_QUERYSUCCESS,
                                String.valueOf(holidays.size()),
                                m.getMessage().getMessage(),
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
                        m.getEvent().getChannel().getName(),
                        Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
                                LineIds.C_HOLIDAY_QUERYSUCCESS,
                                String.valueOf(holidays.size()),
                                m.getMessage().getMessage(),
                                msg
                        )
                );
            }

            return null;
        }

        int month;
        int day;

        ArrayList<String> s = new ArrayList<>(Arrays.asList(m.getMessage().getMessage().split(" ")));

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
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.HTTP_ERROR,
                        String.valueOf(response.code()),
                        "Holiday"
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (holidays.size() == 0) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.C_HOLIDAY_NOHOLIDAYS,
                    StringUtils.pad(day) + "/" + StringUtils.pad(month)
            );
        }

        String name = holidays.get((int) Math.floor(Math.random() * holidays.size() - 1));

        if (m.getMessage().getOptions().contains("all") || m.getMessage().getOptions().contains("все")) {
            return String.join(", ", holidays);
        }

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request statsRequest = new Request.Builder()
                .get()
                .url(SharedConstants.STATS_URL + "/api/v1/channel/" + m.getEvent().getChannel().getId() + "/emotes")
                .build();

        ArrayList<Emote> emotes;

        try (Response response = client.newCall(statsRequest).execute()) {
            if (response.code() != 200) {
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.HTTP_ERROR,
                        String.valueOf(response.code()),
                        "Stats API"
                );
            }

            if (response.body() == null) {
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.SOMETHING_WENT_WRONG
                );
            }

            String body = response.body().string();

            ServerPayload<ArrayList<Emote>> payload = new Gson().fromJson(body, new TypeToken<ServerPayload<ArrayList<Emote>>>(){}.getType());

            if (payload.getData() != null) {
                emotes = payload.getData();
            } else {
                return Huinyabot.getInstance().getLocale().formattedText(
                        m.getLanguage(),
                        LineIds.C_ETOP_NOCHANNELEMOTES,
                        Huinyabot.getInstance().getLocale().literalText(
                                m.getLanguage(),
                                LineIds.STV
                        ),
                        Huinyabot.getInstance().getLocale().literalText(
                                m.getLanguage(),
                                LineIds.STV
                        )
                );
            }
        } catch (IOException e) {
            return Huinyabot.getInstance().getLocale().formattedText(
                    m.getLanguage(),
                    LineIds.SOMETHING_WENT_WRONG
            );
        }

        if (m.getMessage().getOptions().contains("massping") || m.getMessage().getOptions().contains("тык")) {
            ArrayList<String> msgs = new ArrayList<>();
            int index = 0;
            Chatters chatters = Huinyabot.getInstance().getClient().getMessagingInterface().getChatters(m.getEvent().getChannel().getName()).execute();

            msgs.add("");

            for (String uName : chatters.getAllViewers()) {
                StringBuilder sb = new StringBuilder();

                if (!m.getMessage().getOptions().contains("no-emotes") || !m.getMessage().getOptions().contains("без-эмоутов")) {
                    String finalUName = uName;
                    Optional<Emote> optionalEmote = emotes.stream().filter(e -> e.getName().equalsIgnoreCase(finalUName)).findFirst();

                    if (optionalEmote.isPresent()) {
                        uName = optionalEmote.get().getName();
                    }
                }

                if (
                        Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
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
                        m.getEvent().getChannel().getName(),
                        Huinyabot.getInstance().getLocale().formattedText(
                                m.getLanguage(),
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

        return Huinyabot.getInstance().getLocale().formattedText(
                m.getLanguage(),
                LineIds.C_HOLIDAY_SUCCESS,
                "",
                StringUtils.pad(day) + "/" + StringUtils.pad(month),
                String.valueOf(holidays.indexOf(name) + 1),
                String.valueOf(holidays.size()),
                name
        );
    }
}
