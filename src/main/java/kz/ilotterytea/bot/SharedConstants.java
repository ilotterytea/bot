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
    public static final String CHAIN_SAVE_PATH = "chains.json";

    public static final File USERS_DIR = new File(USER_SAVE_PATH);
    public static final File TARGETS_DIR = new File(TARGET_SAVE_PATH);

    public static final File CHAINS_FILE = new File(CHAIN_SAVE_PATH);

    public static final String DEFAULT_PREFIX = "!";
    public static final String DEFAULT_LOCALE_ID = "en_us";

    public static final String USER_AGENT = String.format("%s/%s", APP_NAME, getVersion());

    public static final String HOLIDAY_URL = "https://hol.ilotterytea.kz/api/v1/%s/%s";
    public static final String HOLIDAY_SEARCH_URL = "https://hol.ilotterytea.kz/api/v1/search?q=%s";

    public static final String STV_EVENTAPI_ENDPOINT_URL = "wss://events.7tv.app/v1/channel-emotes";
    public static final String STV_CHANNEL_EMOTES_URL = "https://api.7tv.app/v2/users/%s/emotes";
    public static final String STV_GLOBAL_EMOTES_URL = "https://api.7tv.app/v2/emotes/global";

    public static final String NEUROBAJ_URL = "https://neurobaj.ilotterytea.kz";
}
