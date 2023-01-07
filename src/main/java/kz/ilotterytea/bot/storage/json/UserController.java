package kz.ilotterytea.bot.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.models.UserModel;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * User controller.
 * @author ilotterytea
 * @since 1.0
 */
public class UserController implements JsonController<UserModel> {
    private final Map<String, UserModel> models;

    public UserController(File directory) {
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
            UserModel model = new Gson().fromJson(reader, UserModel.class);

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
    public UserModel get(String key) {
        if (models.containsKey(key)) return models.get(key);
        return null;
    }

    @Override
    public UserModel getOrDefault(String key) {
        if (models.containsKey(key)) return models.get(key);
        return genDefault(key);
    }

    @Override
    public void set(String key, UserModel obj) {
        models.put(key, obj);
    }

    @Override
    public void save() {
        for (UserModel model : models.values()) {
            saveFile(model);
        }
    }

    @Override
    public Map<String, UserModel> getAll() {
        return models;
    }

    private void saveFile(UserModel model) {
        try (Writer writer = new FileWriter(String.format("%s/%s.json", SharedConstants.USER_SAVE_PATH, model.getAliasId()))) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(model, UserModel.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UserModel genDefault(String aliasId) {
        return new UserModel(
                aliasId,
                false,
                false
        );
    }
}
