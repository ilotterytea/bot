package kz.ilotterytea.bot;

import java.io.File;

/**
 * App constants.
 * @author ilotterytea
 * @since 1.0
 */
public class SharedConstants {
    public static final String PROPERTIES_PATH = "config.properties";
    public static final String USER_SAVE_PATH = "./users";
    public static final String TARGET_SAVE_PATH = "./targets";

    public static final File USERS_DIR = new File(USER_SAVE_PATH);
    public static final File TARGETS_DIR = new File(TARGET_SAVE_PATH);

    public static final String DEFAULT_PREFIX = "!";
}
