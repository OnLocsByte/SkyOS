package net.skyos.core.api.module;

public interface SkyOSModule {

    String getId();

    String getDisplayName();

    String getDescription();

    ModuleCategory getCategory();

    void onEnable();

    void onDisable();

    default boolean isEnabled() {
        return ModuleRegistry.getInstance().isEnabled(getId());
    }

    default void toggle() {
        ModuleRegistry.getInstance().setEnabled(getId(), !isEnabled());
    }
}
