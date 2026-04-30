package net.skyos.core.api.module;

import net.skyos.core.config.SkyOSConfig;
import net.skyos.core.event.EventBus;
import net.skyos.core.SkyOSCoreClient;

import java.util.*;

public final class ModuleRegistry {

    private static ModuleRegistry instance;

    private final EventBus eventBus;
    private final SkyOSConfig config;
    private final Map<String, SkyOSModule> modules = new LinkedHashMap<>();
    private final Map<String, Boolean> enabledState = new HashMap<>();

    public ModuleRegistry(EventBus eventBus, SkyOSConfig config) {
        this.eventBus = eventBus;
        this.config = config;
        instance = this;
    }

    public static ModuleRegistry getInstance() {
        return instance;
    }

    public void register(SkyOSModule module) {
        if (modules.containsKey(module.getId())) {
            SkyOSCoreClient.LOGGER.warn("Module '{}' is already registered, skipping.", module.getId());
            return;
        }
        modules.put(module.getId(), module);

        boolean enabled = config.getModuleEnabled(module.getId(), true);
        enabledState.put(module.getId(), enabled);

        if (enabled) {
            try {
                module.onEnable();
            } catch (Exception e) {
                SkyOSCoreClient.LOGGER.error("Failed to enable module '{}'", module.getId(), e);
                enabledState.put(module.getId(), false);
            }
        }
        SkyOSCoreClient.LOGGER.info("Registered module: {} (enabled={})", module.getId(), enabled);
    }

    public void unregister(String moduleId) {
        SkyOSModule module = modules.remove(moduleId);
        if (module == null) return;
        if (Boolean.TRUE.equals(enabledState.remove(moduleId))) {
            try {
                module.onDisable();
            } catch (Exception e) {
                SkyOSCoreClient.LOGGER.error("Failed to disable module '{}' during unregister", moduleId, e);
            }
        }
    }

    public void setEnabled(String moduleId, boolean enabled) {
        SkyOSModule module = modules.get(moduleId);
        if (module == null) return;

        boolean current = Boolean.TRUE.equals(enabledState.get(moduleId));
        if (current == enabled) return;

        enabledState.put(moduleId, enabled);
        config.setModuleEnabled(moduleId, enabled);

        try {
            if (enabled) module.onEnable();
            else module.onDisable();
        } catch (Exception e) {
            SkyOSCoreClient.LOGGER.error("Error toggling module '{}'", moduleId, e);
        }
    }

    public boolean isEnabled(String moduleId) {
        return Boolean.TRUE.equals(enabledState.get(moduleId));
    }

    public Optional<SkyOSModule> getModule(String id) {
        return Optional.ofNullable(modules.get(id));
    }

    public Collection<SkyOSModule> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public List<SkyOSModule> getModulesByCategory(ModuleCategory category) {
        return modules.values().stream()
                .filter(m -> m.getCategory() == category)
                .toList();
    }
}
