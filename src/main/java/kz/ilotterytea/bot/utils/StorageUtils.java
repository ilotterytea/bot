package kz.ilotterytea.bot.utils;

import kz.ilotterytea.bot.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Storage utilities.
 * @author ilotterytea
 * @since 1.0
 */
public class StorageUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(StorageUtils.class);

    /**
     * Check the integrity of important directories and files for the bot.
     */
    public static void checkIntegrity() {
        File usersDir = SharedConstants.USERS_DIR;
        File targetsDir = SharedConstants.TARGETS_DIR;

        if (!usersDir.exists()) {
            LOGGER.debug(
                    String.format(
                            "%s the directory for users (%s)!",
                            (usersDir.mkdirs()) ? "Successfully created" : "Cannot create",
                            usersDir.getPath()
                    )
            );
        }

        if (!targetsDir.exists()) {
            LOGGER.debug(
                    String.format(
                            "%s the directory for targets (%s)!",
                            (targetsDir.mkdirs()) ? "Successfully created" : "Cannot create",
                            targetsDir.getPath()
                    )
            );
        }
    }

    public File getFileFromResource(String file_path) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource(file_path);

            if (resource != null) {
                return new File(resource.toURI());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
