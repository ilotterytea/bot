package kz.ilotterytea.bot.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kz.ilotterytea.bot.utils.StorageUtils;

import java.util.*;

/**
 * Localization manager.
 * @author ilotterytea
 * @since 1.3
 */
public class I18N {
    private final Map<String, Map<String, String>> maps;

    public I18N(List<String> filepaths) {
        this.maps = new HashMap<>();

        load(filepaths);
    }

    private void load(List<String> filepaths) {

        for (String filepath : filepaths) {
            processFile(filepath);
        }
    }

    private void processFile(String filepath) {
        if (filepath.startsWith("/")) {
            filepath = filepath.substring(1);
        }

        String string = StorageUtils.readFileFromResources(filepath);

        Map<String, String> l = new Gson().fromJson(string, new TypeToken<Map<String, String>>(){}.getType());

        if (l != null) {
            maps.put(filepath.split("/")[1].split("\\.")[0], l);
        }
    }

    /**
     * Get a totally clean text.
     * @param localeId Locale ID.
     * @param lineId Line ID.
     * @return String if line ID & locale ID exist, otherwise null.
     */
    public String literalText(String localeId, LineIds lineId) {
        if (!maps.containsKey(localeId) || !maps.get(localeId).containsKey(lineId.getId())) {
            return null;
        }

        return maps.get(localeId).get(lineId.getId());
    }

    /**
     * Get the parsed text. All %s in the text will be replaced by the provided parameters.
     * @param localeId Locale ID.
     * @param lineId Line ID.
     * @param params Parameters.
     * @return String if line ID & locale ID exist, otherwise null.
     */
    public String formattedText(String localeId, LineIds lineId, String... params) {
        String text = literalText(localeId, lineId);

        if (text == null) {
            return null;
        }

        ArrayList<String> s = new ArrayList<>();
        int index = 0;

        for (String w : text.split(" ")) {
            if (w.contains("%s") && index <= params.length - 1) {
                w = w.replace("%s", params[index]);
                index++;
            }

            s.add(w);
        }

        return String.join(" ", s);
    }

    public ArrayList<String> getLocaleIds() { return new ArrayList<>(this.maps.keySet()); }
}
