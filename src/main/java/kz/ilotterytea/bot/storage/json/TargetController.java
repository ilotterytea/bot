package kz.ilotterytea.bot.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.models.StatsModel;
import kz.ilotterytea.bot.models.TargetModel;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Target controller.
 * @author ilotterytea
 * @since 1.0
 */
public class TargetController implements JsonController<TargetModel> {
    private final Map<String, TargetModel> models;

    public TargetController(File directory) {
        this.models = new HashMap<>();
        load(directory);
    }

    @Override
    public void load(File file) {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles(file1 -> file1.getName().endsWith(".json")))) {
                if (f.isDirectory()) continue;
                processFile(f);
            }
        } else {
            processFile(file);
        }
    }

    private void processFile(File file) {
        try (Reader reader = new FileReader(file)){
            TargetModel model = new Gson().fromJson(reader, TargetModel.class);

            if (model == null) {
                model = genDefault(file.getName().split("\\.")[0]);
                saveFile(model);
            }

            models.put(model.getAliasId(), model);
        } catch (IOException e) {
            e.printStackTrace();

            models.put(file.getName().split("\\.")[0], genDefault(file.getName().split("\\.")[0]));
        }
    }

    @Override
    public TargetModel get(String key) {
        if (models.containsKey(key)) return models.get(key);
        return null;
    }

    @Override
    public void set(String key, TargetModel obj) {
        models.put(key, obj);
    }

    @Override
    public void save() {
        for (TargetModel model : models.values()) {
            saveFile(model);
        }
    }

    @Override
    public TargetModel getOrDefault(String key) {
        if (models.containsKey(key)) return models.get(key);
        return genDefault(key);
    }

    @Override
    public Map<String, TargetModel> getAll() {
        return models;
    }

    private void saveFile(TargetModel model) {
        try (Writer writer = new FileWriter(String.format("%s/%s.json", SharedConstants.TARGET_SAVE_PATH, model.getAliasId()))) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(model, TargetModel.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TargetModel genDefault(String aliasId) {
        return new TargetModel(
                aliasId,
                new StatsModel(
                        0,
                        0,
                        0
                ),
                false,
                SharedConstants.DEFAULT_LOCALE_ID,
                new HashMap<>(),
                new HashMap<>()
        );
    }
}
