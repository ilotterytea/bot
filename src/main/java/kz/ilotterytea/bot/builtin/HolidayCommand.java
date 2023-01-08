package kz.ilotterytea.bot.builtin;

import com.github.twitch4j.tmi.domain.Chatters;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.api.commands.Command;
import kz.ilotterytea.bot.api.permissions.Permissions;
import kz.ilotterytea.bot.models.ArgumentsModel;
import kz.ilotterytea.bot.models.emotes.Emote;
import kz.ilotterytea.bot.models.emotes.Provider;
import kz.ilotterytea.bot.net.HttpFactory;
import kz.ilotterytea.bot.net.models.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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
    public ArrayList<String> getSubcommands() { return new ArrayList<>(); }

    @Override
    public ArrayList<String> getAliases() { return new ArrayList<>(Arrays.asList("праздник")); }

    @Override
    public String run(ArgumentsModel m) {
        Response response = HttpFactory.sendGETRequest(SharedConstants.HOLIDAY_URL);

        if (response == null) {
            return "Something went wrong!";
        }

        if (response.getResponse() == null ){
            return "Received the "+response.getCode()+" status code while processing the "+response.getMethod()+" request!";
        }

        Document doc = Jsoup.parse(response.getResponse());
        Element hdaylist = doc.getElementsByClass("holidays-items").get(0);

        Elements holidays = hdaylist.getElementsByTag("li");
        ArrayList<String> names = new ArrayList<>();

        for (Element e : holidays) {

            Elements anchors = e.getElementsByTag("a");

            if (anchors.size() > 0 && anchors.get(0) != null) {
                names.add(anchors.get(0).ownText());
            } else {
                Element span = e.getElementsByTag("span").get(0);
                names.add(span.ownText());
            }

        }

        String name = names.get((int) Math.floor(Math.random() * names.size() - 1));

        if (m.getMessage().getOptions().contains("all") || m.getMessage().getOptions().contains("все")) {
            return String.join(", ", names);
        }

        if (m.getMessage().getOptions().contains("massping") || m.getMessage().getOptions().contains("тык")) {
            ArrayList<String> msgs = new ArrayList<>();
            int index = 0;
            Chatters chatters = Huinyabot.getInstance().getClient().getMessagingInterface().getChatters(m.getEvent().getChannel().getName()).execute();

            msgs.add("");

            for (String uName : chatters.getAllViewers()) {
                StringBuilder sb = new StringBuilder();

                if (!m.getMessage().getOptions().contains("no-emotes") || !m.getMessage().getOptions().contains("без-эмоутов")) {
                    if (Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getEmotes().containsKey(Provider.SEVENTV)) {
                        for (Emote emote : Huinyabot.getInstance().getTargetCtrl().get(m.getEvent().getChannel().getId()).getEmotes().get(Provider.SEVENTV).values()) {
                            if (Objects.equals(emote.getName().toLowerCase(), uName.toLowerCase())) {
                                uName = emote.getName();
                                break;
                            }
                        }
                    }
                }

                if (
                        new StringBuilder()
                                .append(msgs.get(index))
                                .append(uName)
                                .append(" ")
                                .append("Today's holiday: ")
                                .append(name)
                                .append(" HolidayPresent")
                                .length() < 500
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
                        msg + "Today's holiday: " + name + " HolidayPresent"
                );
            }

            return null;
        }

        return name;
    }
}
