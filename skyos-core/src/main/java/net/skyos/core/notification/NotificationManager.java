package net.skyos.core.notification;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.skyos.core.overlay.OverlayManager;
import net.skyos.core.render.RenderEngine;
import net.skyos.core.render.theme.SkyOSPalette;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class NotificationManager {

    private static final int MAX_VISIBLE = 5;
    private static final int NOTIF_WIDTH = 280;
    private static final int NOTIF_HEIGHT = 56;
    private static final int NOTIF_PADDING = 6;
    private static final int MARGIN_RIGHT = 8;
    private static final int MARGIN_BOTTOM = 8;
    private static final float ANIM_SPEED = 0.12f;

    private final OverlayManager overlayManager;
    private final List<Notification> active = new ArrayList<>();
    private final Deque<Notification> queue = new ArrayDeque<>();

    private long lastTickMs = System.currentTimeMillis();

    public NotificationManager(OverlayManager overlayManager) {
        this.overlayManager = overlayManager;
    }

    public void push(String title, String message, NotificationType type) {
        push(title, message, type, 4000L);
    }

    public void push(String title, String message, NotificationType type, long durationMs) {
        Notification notif = new Notification(title, message, type, durationMs);
        if (active.size() < MAX_VISIBLE) {
            active.add(notif);
        } else {
            queue.add(notif);
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        float delta = now - lastTickMs;
        lastTickMs = now;

        active.removeIf(n -> {
            if (n.isExpired() || n.isDismissed()) {
                if (!queue.isEmpty() && active.size() < MAX_VISIBLE) {
                    active.add(queue.poll());
                }
                return true;
            }
            return false;
        });

        float targetAlpha;
        float targetSlide = 0f;
        for (Notification n : active) {
            float life = n.getLifeProgress();
            if (life < 0.1f) {
                targetAlpha = life / 0.1f;
            } else if (life > 0.85f) {
                targetAlpha = 1f - (life - 0.85f) / 0.15f;
            } else {
                targetAlpha = 1f;
            }
            n.setAlpha(lerp(n.getAlpha(), targetAlpha, ANIM_SPEED * delta / 16f));
            n.setSlideOffset(lerp(n.getSlideOffset(), targetSlide, ANIM_SPEED * delta / 16f));
        }
    }

    public void render(DrawContext ctx, float tickDelta) {
        if (active.isEmpty()) return;

        RenderEngine re = overlayManager.getRenderEngine();
        int sw = re.getScaledWidth();
        int sh = re.getScaledHeight();

        int y = sh - MARGIN_BOTTOM;

        for (int i = active.size() - 1; i >= 0; i--) {
            Notification n = active.get(i);
            y -= NOTIF_HEIGHT;

            int alpha = (int) (n.getAlpha() * 255);
            if (alpha <= 0) { y -= NOTIF_PADDING; continue; }

            int x = (int) (sw - NOTIF_WIDTH - MARGIN_RIGHT + n.getSlideOffset());

            // Background
            re.fillRounded(ctx, x, y, NOTIF_WIDTH, NOTIF_HEIGHT, 5,
                    SkyOSPalette.withAlpha(SkyOSPalette.BG_CARD, alpha));

            // Accent left bar
            re.fillRect(ctx, x, y + 4, 3, NOTIF_HEIGHT - 8,
                    SkyOSPalette.withAlpha(n.getType().accentColor, alpha));

            // Glow on accent bar
            re.drawGlow(ctx, x, y + 4, 3, NOTIF_HEIGHT - 8,
                    SkyOSPalette.withAlpha(n.getType().accentColor, alpha / 3), 2);

            // Border
            re.drawBorder(ctx, x, y, NOTIF_WIDTH, NOTIF_HEIGHT, 1,
                    SkyOSPalette.withAlpha(SkyOSPalette.BORDER_SUBTLE, alpha));

            // Type label
            MinecraftClient mc = MinecraftClient.getInstance();
            int textAlpha = alpha;
            ctx.drawTextWithShadow(mc.textRenderer,
                    n.getType().label,
                    x + 10, y + 8,
                    SkyOSPalette.withAlpha(n.getType().accentColor, textAlpha));

            // Title
            ctx.drawTextWithShadow(mc.textRenderer,
                    n.getTitle(),
                    x + 10, y + 20,
                    SkyOSPalette.withAlpha(SkyOSPalette.TEXT_PRIMARY, textAlpha));

            // Message
            ctx.drawTextWithShadow(mc.textRenderer,
                    n.getMessage(),
                    x + 10, y + 32,
                    SkyOSPalette.withAlpha(SkyOSPalette.TEXT_SECONDARY, textAlpha));

            // Progress bar (life remaining)
            float remaining = 1f - n.getLifeProgress();
            re.fillRect(ctx, x + 1, y + NOTIF_HEIGHT - 3, NOTIF_WIDTH - 2, 2,
                    SkyOSPalette.withAlpha(SkyOSPalette.BG_SECONDARY, alpha));
            int barW = (int) ((NOTIF_WIDTH - 2) * remaining);
            if (barW > 0) {
                re.fillRect(ctx, x + 1, y + NOTIF_HEIGHT - 3, barW, 2,
                        SkyOSPalette.withAlpha(n.getType().accentColor, alpha));
            }

            y -= NOTIF_PADDING;
        }
    }

    private float lerp(float a, float b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return a + (b - a) * t;
    }
}
