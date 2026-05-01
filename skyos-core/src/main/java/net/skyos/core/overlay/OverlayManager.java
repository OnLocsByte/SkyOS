package net.skyos.core.overlay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.skyos.core.api.overlay.Overlay;
import net.skyos.core.api.overlay.OverlayAnchor;
import net.skyos.core.render.RenderEngine;
import net.skyos.core.SkyOSCoreClient;

import java.util.*;

public final class OverlayManager {

    private final RenderEngine renderEngine;
    private final Map<String, Overlay> overlays = new LinkedHashMap<>();

    public OverlayManager(RenderEngine renderEngine) {
        this.renderEngine = renderEngine;
    }

    public void register(Overlay overlay) {
        overlays.put(overlay.getId(), overlay);
    }

    public void unregister(String id) {
        overlays.remove(id);
    }

    public Optional<Overlay> get(String id) {
        return Optional.ofNullable(overlays.get(id));
    }

    public void tick(MinecraftClient client) {
        for (Overlay overlay : overlays.values()) {
            try {
                overlay.tick();
            } catch (Exception e) {
                SkyOSCoreClient.LOGGER.error("Error ticking overlay '{}'", overlay.getId(), e);
            }
        }
    }

    public void render(DrawContext ctx, float tickDelta) {
        List<Overlay> sorted = overlays.values().stream()
                .filter(Overlay::isVisible)
                .sorted(Comparator.comparingInt(Overlay::getZIndex))
                .toList();

        int sw = renderEngine.getScaledWidth();
        int sh = renderEngine.getScaledHeight();

        for (Overlay overlay : sorted) {
            try {
                ctx.getMatrices().pushMatrix();
                int[] resolved = resolvePosition(overlay, sw, sh);
                ctx.getMatrices().translate(
                        (float) (resolved[0] - overlay.getX()),
                        (float) (resolved[1] - overlay.getY()));
                overlay.render(ctx, tickDelta);
                ctx.getMatrices().popMatrix();
            } catch (Exception e) {
                SkyOSCoreClient.LOGGER.error("Error rendering overlay '{}'", overlay.getId(), e);
            }
        }
    }

    private int[] resolvePosition(Overlay overlay, int sw, int sh) {
        int x = overlay.getX();
        int y = overlay.getY();
        int w = overlay.getWidth();
        int h = overlay.getHeight();

        return switch (overlay.getAnchor()) {
            case TOP_LEFT      -> new int[]{ x, y };
            case TOP_CENTER    -> new int[]{ sw / 2 - w / 2 + x, y };
            case TOP_RIGHT     -> new int[]{ sw - w + x, y };
            case MIDDLE_LEFT   -> new int[]{ x, sh / 2 - h / 2 + y };
            case MIDDLE_CENTER -> new int[]{ sw / 2 - w / 2 + x, sh / 2 - h / 2 + y };
            case MIDDLE_RIGHT  -> new int[]{ sw - w + x, sh / 2 - h / 2 + y };
            case BOTTOM_LEFT   -> new int[]{ x, sh - h + y };
            case BOTTOM_CENTER -> new int[]{ sw / 2 - w / 2 + x, sh - h + y };
            case BOTTOM_RIGHT  -> new int[]{ sw - w + x, sh - h + y };
            case CUSTOM        -> new int[]{ x, y };
        };
    }

    public RenderEngine getRenderEngine() {
        return renderEngine;
    }

    public Collection<Overlay> getAll() {
        return Collections.unmodifiableCollection(overlays.values());
    }
}
