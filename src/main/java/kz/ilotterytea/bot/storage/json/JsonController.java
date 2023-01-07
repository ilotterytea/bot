package kz.ilotterytea.bot.storage.json;

import java.io.File;
import java.util.Map;

/**
 * JSON controller.
 * @author ilotterytea
 * @since 1.0
 */
public interface JsonController<T> {
    /** Get the item. */
    T get(String key);
    /** Set the item. */
    void set(String key, T obj);

    /** Load the item(s). */
    void load(File file);
    /** Save all the items. */
    void save();

    /** Get the item. If null, then get the default item. */
    T getOrDefault(String key);

    /** Get all the items. */
    Map<String, T> getAll();
}
