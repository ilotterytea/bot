package kz.ilotterytea.bot.builtin.spam;

import kz.ilotterytea.bot.api.commands.*;
import kz.ilotterytea.bot.entities.channels.Channel;
import kz.ilotterytea.bot.entities.permissions.Permission;
import kz.ilotterytea.bot.utils.ParsedMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Spam command.
 *
 * @author ilotterytea
 * @since 1.0
 */
public class SpamCommand implements Command {
    @Override
    public String getNameId() {
        return "spam";
    }

    @Override
    public int getDelay() {
        return 30000;
    }

    @Override
    public Permission getPermissions() {
        return Permission.MOD;
    }

    @Override
    public List<String> getOptions() {
        return Collections.singletonList("count");
    }

    @Override
    public List<String> getAliases() {
        return List.of("спам", "насрать", "repeat", "cv", "paste", "cvpaste");
    }

    @Override
    public Response run(Request request) {
        ParsedMessage message = request.getMessage();
        Channel channel = request.getChannel();

        if (message.getMessage().isEmpty() || message.getMessage().get().split(" ").length == 1) {
            throw CommandException.notEnoughArguments(request, CommandArgument.MESSAGE);
        }

        final int MAX_COUNT = 8;
        ArrayList<String> s = new ArrayList<>(Arrays.asList(message.getMessage().get().split(" ")));
        int count;

        try {
            count = Integer.parseInt(s.get(0));
            s.remove(0);
        } catch (NumberFormatException e) {
            count = MAX_COUNT;
        }

        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }

        ArrayList<String> msgs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            msgs.add(String.format(
                    "%s %s",
                    String.join(" ", s),
                    (message.getUsedOptions().contains("count")) ? i + 1 : ""
            ));
        }

        return Response.ofMultiple(msgs);
    }
}
