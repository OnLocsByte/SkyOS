package net.skyos.core.config;

import com.google.gson.*;
import net.skyos.core.SkyOSCoreClient;
import net.skyos.core.storage.StorageManager;

import java.util.*;

public final class SkyOSConfig {

    private static final String CONFIG_KEY = "skyos-core-config";

    private final StorageManager storage;
    private JsonObject root;

    public SkyOSConfig(StorageManager storage) {
        this.storage = storage;
        this.root = new JsonObject();
    }

    public void load() {
        try {
            String raw = storage.readString(CONFIG_KEY);
            if (raw != null && !raw.isBlank()) {
                root = JsonParser.parseString(raw).getAsJsonObject();
            }
        } catch (Exception e) {
            SkyOSCoreClient.LOGGER.warn("Failed to load config, using defaults.", e);
            root = new JsonObject();
        }
    }

    public void save() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            storage.writeString(CONFIG_KEY, gson.toJson(root));
        } catch (Exception e) {
            SkyOSCoreClient.LOGGER.error("Failed to save config.", e);
        }
    }

    public boolean getModuleEnabled(String moduleId, boolean defaultValue) {
        JsonObject modules = getOrCreateObject("modules");
        if (modules.has(moduleId)) {
            return modules.get(moduleId).getAsBoolean();
        }
        return defaultValue;
    }

    public void setModuleEnabled(String moduleId, boolean enabled) {
        getOrCreateObject("modules").addProperty(moduleId, enabled);
    }

    public String getString(String key, String defaultValue) {
        return root.has(key) ? root.get(key).getAsString() : defaultValue;
    }

    public void setString(String key, String value) {
        root.addProperty(key, value);
    }

    public int getInt(String key, int defaultValue) {
        return root.has(key) ? root.get(key).getAsInt() : defaultValue;
    }

    public void setInt(String key, int value) {
        root.addProperty(key, value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return root.has(key) ? root.get(key).getAsBoolean() : defaultValue;
    }

    public void setBoolean(String key, boolean value) {
        root.addProperty(key, value);
    }

    public float getFloat(String key, float defaultValue) {
        return root.has(key) ? root.get(key).getAsFloat() : defaultValue;
    }

    public void setFloat(String key, float value) {
        root.addProperty(key, value);
    }

    public JsonObject getSection(String section) {
        return getOrCreateObject(section);
    }

    private JsonObject getOrCreateObject(String key) {
        if (!root.has(key) || !root.get(key).isJsonObject()) {
            root.add(key, new JsonObject());
        }
        return root.getAsJsonObject(key);
    }
}
