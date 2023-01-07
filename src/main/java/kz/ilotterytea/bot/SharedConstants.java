package kz.ilotterytea.bot;

import java.io.File;

/**
 * App constants.
 * @author ilotterytea
 * @since 1.0
 */
public class SharedConstants {
    public static final String APP_NAME = "huinyabot";
    public static final byte APP_VERSION_MAJOR = 1;
    public static final byte APP_VERSION_MINOR = 0;
    public static final byte APP_VERSION_PATCH = 0;

    public static String getVersion() {
        return String.format("%s.%s.%s", APP_VERSION_MAJOR, APP_VERSION_MINOR, APP_VERSION_PATCH);
    }

    public static final String PROPERTIES_PATH = "config.properties";
    public static final String USER_SAVE_PATH = "./users";
    public static final String TARGET_SAVE_PATH = "./targets";

    public static final File USERS_DIR = new File(USER_SAVE_PATH);
    public static final File TARGETS_DIR = new File(TARGET_SAVE_PATH);

    public static final String DEFAULT_PREFIX = "!";

    public static final String USER_AGENT = String.format("%s/%s", APP_NAME, getVersion());

    public static final String HOLIDAY_URL = "https://my-calend.ru/holidays";
}
