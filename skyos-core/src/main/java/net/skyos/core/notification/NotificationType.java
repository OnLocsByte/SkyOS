package net.skyos.core.notification;

import net.skyos.core.render.theme.SkyOSPalette;

public enum NotificationType {
    INFO   (SkyOSPalette.STATUS_INFO,    "Info"),
    SUCCESS(SkyOSPalette.STATUS_SUCCESS, "Success"),
    WARNING(SkyOSPalette.STATUS_WARNING, "Warning"),
    ERROR  (SkyOSPalette.STATUS_ERROR,   "Error");

    final int accentColor;
    final String label;

    NotificationType(int accentColor, String label) {
        this.accentColor = accentColor;
        this.label = label;
    }
}
