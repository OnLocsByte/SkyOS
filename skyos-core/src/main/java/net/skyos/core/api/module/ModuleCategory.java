package net.skyos.core.api.module;

public enum ModuleCategory {
    RENDERING("Rendering"),
    OVERLAY("Overlay"),
    HUD("HUD"),
    INVENTORY("Inventory"),
    DUNGEONS("Dungeons"),
    ECONOMY("Economy"),
    UTILITY("Utility"),
    MISCELLANEOUS("Miscellaneous");

    private final String displayName;

    ModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
