package net.skyos.core.api.overlay;

import net.minecraft.client.gui.DrawContext;

public interface Overlay {

    String getId();

    OverlayAnchor getAnchor();

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    boolean isVisible();

    void render(DrawContext ctx, float tickDelta);

    default void tick() {}

    default int getZIndex() { return 0; }

    default boolean isDraggable() { return false; }
}
