package net.skyos.core.storage;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.skyos.core.SkyOSCoreClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class StorageManager {

    private final Path dataDir;
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public StorageManager() {
        this.dataDir = FabricLoader.getInstance().getConfigDir().resolve("skyos");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            SkyOSCoreClient.LOGGER.error("Failed to create SkyOS config directory", e);
        }
    }

    public void writeString(String key, String value) {
        cache.put(key, value);
        Path file = dataDir.resolve(sanitizeKey(key) + ".json");
        try {
            Files.writeString(file, value, StandardCharsets.UTF_8);
        } catch (IOException e) {
            SkyOSCoreClient.LOGGER.error("Failed to write storage key '{}'", key, e);
        }
    }

    public String readString(String key) {
        if (cache.containsKey(key)) return cache.get(key);
        Path file = dataDir.resolve(sanitizeKey(key) + ".json");
        if (!Files.exists(file)) return null;
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            cache.put(key, content);
            return content;
        } catch (IOException e) {
            SkyOSCoreClient.LOGGER.error("Failed to read storage key '{}'", key, e);
            return null;
        }
    }

    public <T> void writeJson(String key, T value) {
        writeString(key, gson.toJson(value));
    }

    public <T> Optional<T> readJson(String key, Class<T> type) {
        String raw = readString(key);
        if (raw == null || raw.isBlank()) return Optional.empty();
        try {
            return Optional.ofNullable(gson.fromJson(raw, type));
        } catch (JsonSyntaxException e) {
            SkyOSCoreClient.LOGGER.warn("Corrupt storage data for key '{}', ignoring.", key);
            return Optional.empty();
        }
    }

    public void delete(String key) {
        cache.remove(key);
        Path file = dataDir.resolve(sanitizeKey(key) + ".json");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            SkyOSCoreClient.LOGGER.warn("Failed to delete storage key '{}'", key, e);
        }
    }

    public void flush() {
        for (Map.Entry<String, String> entry : cache.entrySet()) {
            Path file = dataDir.resolve(sanitizeKey(entry.getKey()) + ".json");
            try {
                Files.writeString(file, entry.getValue(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                SkyOSCoreClient.LOGGER.error("Failed to flush storage key '{}'", entry.getKey(), e);
            }
        }
    }

    private String sanitizeKey(String key) {
        return key.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    public Path getDataDir() {
        return dataDir;
    }
}
