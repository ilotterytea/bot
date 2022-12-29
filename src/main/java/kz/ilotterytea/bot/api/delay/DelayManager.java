package kz.ilotterytea.bot.api.delay;

import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.api.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Delay manager.
 * @author ilotterytea
 * @since 1.0
 */
public class DelayManager {
    private Map<String, ArrayList<String>> DELAYED;
    private Logger LOGGER = LoggerFactory.getLogger(DelayManager.class);

    public DelayManager() {
        DELAYED = new HashMap<>();
    }

    /**
     * Delay the user from using the command.
     * @since 1.0
     * @author ilotterytea
     * @param nameId Command name ID.
     * @param userId User ID.
     */
    public void delay(String nameId, String userId) {
        Command cmd = Huinyabot.getLoader().getCommands().get(nameId);

        if (cmd == null) {
            LOGGER.warn("No command with ID " + nameId + " found!");
            return;
        }
        if (!DELAYED.containsKey(cmd.getNameId())) DELAYED.put(cmd.getNameId(), new ArrayList<>());

        DELAYED.get(cmd.getNameId()).add(userId);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                DELAYED.get(cmd.getNameId()).remove(userId);
            }
        }, cmd.getDelay());

        LOGGER.debug(String.format("User ID %s delayed for %ss from using %s command!", userId, cmd.getDelay() / 1000, nameId));
    }

    /**
     * Is user ID delayed?
     * @since 1.0
     * @author ilotterytea
     * @param nameId Command name ID
     * @param userId User ID
     * @return true if delayed.
     */
    public boolean isDelayed(String nameId, String userId) {
        return DELAYED.containsKey(nameId) && DELAYED.get(nameId).contains(userId);
    }
}
