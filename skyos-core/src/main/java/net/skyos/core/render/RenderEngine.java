package net.skyos.core.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.skyos.core.render.animation.AnimationEngine;
import net.skyos.core.render.theme.SkyOSPalette;
import org.joml.Matrix4f;

/**
 * SkyOS primary render engine.
 * Provides all primitive drawing operations with the SkyOS visual style.
 */
public final class RenderEngine {

    private final AnimationEngine animationEngine = new AnimationEngine();

    public RenderEngine() {}

    public AnimationEngine getAnimationEngine() {
        return animationEngine;
    }

    // ─── Solid Rectangles ────────────────────────────────────────────────────

    public void fillRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    // ─── Gradient Rectangles ─────────────────────────────────────────────────

    public void fillGradientV(DrawContext ctx, int x, int y, int w, int h, int topColor, int bottomColor) {
        ctx.fillGradient(x, y, x + w, y + h, topColor, bottomColor);
    }

    public void fillGradientH(DrawContext ctx, int x, int y, int w, int h, int leftColor, int rightColor) {
        MatrixStack matrices = ctx.getMatrices();
        matrices.push();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        int r1 = (leftColor >> 16) & 0xFF, g1 = (leftColor >> 8) & 0xFF, b1 = leftColor & 0xFF, a1 = (leftColor >> 24) & 0xFF;
        int r2 = (rightColor >> 16) & 0xFF, g2 = (rightColor >> 8) & 0xFF, b2 = rightColor & 0xFF, a2 = (rightColor >> 24) & 0xFF;

        buf.vertex(matrix, x,     y,     0).color(r1, g1, b1, a1);
        buf.vertex(matrix, x,     y + h, 0).color(r1, g1, b1, a1);
        buf.vertex(matrix, x + w, y + h, 0).color(r2, g2, b2, a2);
        buf.vertex(matrix, x + w, y,     0).color(r2, g2, b2, a2);

        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
        matrices.pop();
    }

    // ─── Rounded Rectangles ──────────────────────────────────────────────────

    public void fillRounded(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        int r = Math.min(radius, Math.min(w, h) / 2);
        // Center fill
        ctx.fill(x + r, y,     x + w - r, y + h, color);
        // Left/right side fills
        ctx.fill(x,     y + r, x + r,     y + h - r, color);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);
        // Corners
        fillCorner(ctx, x + r,         y + r,         r, 180, color);
        fillCorner(ctx, x + w - r,     y + r,         r, 270, color);
        fillCorner(ctx, x + r,         y + h - r,     r, 90,  color);
        fillCorner(ctx, x + w - r,     y + h - r,     r, 0,   color);
    }

    private void fillCorner(DrawContext ctx, int cx, int cy, int r, int startAngle, int color) {
        int segments = Math.max(4, r);
        for (int i = 0; i < segments; i++) {
            double a1 = Math.toRadians(startAngle + (90.0 / segments) * i);
            double a2 = Math.toRadians(startAngle + (90.0 / segments) * (i + 1));
            int x1 = (int) (cx + Math.cos(a1) * r);
            int y1 = (int) (cy - Math.sin(a1) * r);
            int x2 = (int) (cx + Math.cos(a2) * r);
            int y2 = (int) (cy - Math.sin(a2) * r);
            // Fill triangle from center to arc edge
            ctx.fill(Math.min(cx, Math.min(x1, x2)), Math.min(cy, Math.min(y1, y2)),
                     Math.max(cx, Math.max(x1, x2)), Math.max(cy, Math.max(y1, y2)), color);
        }
    }

    // ─── Borders ─────────────────────────────────────────────────────────────

    public void drawBorder(DrawContext ctx, int x, int y, int w, int h, int thickness, int color) {
        ctx.fill(x,             y,              x + w,         y + thickness, color);
        ctx.fill(x,             y + h - thickness, x + w,     y + h,         color);
        ctx.fill(x,             y + thickness,  x + thickness, y + h - thickness, color);
        ctx.fill(x + w - thickness, y + thickness, x + w,     y + h - thickness, color);
    }

    // ─── Glow Effects ────────────────────────────────────────────────────────

    public void drawGlow(DrawContext ctx, int x, int y, int w, int h, int glowColor, int layers) {
        int baseAlpha = (glowColor >> 24) & 0xFF;
        for (int i = layers; i >= 1; i--) {
            int spread = i * 2;
            int alpha = (int) (baseAlpha * ((float) (layers - i + 1) / (layers * 2)));
            int layerColor = SkyOSPalette.withAlpha(glowColor, alpha);
            fillRect(ctx, x - spread, y - spread, w + spread * 2, h + spread * 2, layerColor);
        }
    }

    public void drawGlowBorder(DrawContext ctx, int x, int y, int w, int h, int glowColor) {
        int a = (glowColor >> 24) & 0xFF;
        drawBorder(ctx, x - 1, y - 1, w + 2, h + 2, 1, SkyOSPalette.withAlpha(glowColor, a / 3));
        drawBorder(ctx, x,     y,     w,     h,     1, SkyOSPalette.withAlpha(glowColor, a));
        drawBorder(ctx, x + 1, y + 1, w - 2, h - 2, 1, SkyOSPalette.withAlpha(glowColor, a / 2));
    }

    // ─── Panel (Card) ────────────────────────────────────────────────────────

    public void drawPanel(DrawContext ctx, int x, int y, int w, int h) {
        drawGlow(ctx, x, y, w, h, SkyOSPalette.ACCENT_GLOW_SOFT, 3);
        fillRounded(ctx, x, y, w, h, 6, SkyOSPalette.BG_CARD);
        drawGlowBorder(ctx, x, y, w, h, SkyOSPalette.BORDER_SUBTLE);
    }

    public void drawPanelAccented(DrawContext ctx, int x, int y, int w, int h) {
        drawGlow(ctx, x, y, w, h, SkyOSPalette.ACCENT_GLOW, 4);
        fillRounded(ctx, x, y, w, h, 6, SkyOSPalette.BG_ELEVATED);
        drawGlowBorder(ctx, x, y, w, h, SkyOSPalette.BORDER_ACTIVE);
    }

    // ─── Progress Bar ────────────────────────────────────────────────────────

    public void drawProgressBar(DrawContext ctx, int x, int y, int w, int h, float progress, int fillColor) {
        fillRect(ctx, x, y, w, h, SkyOSPalette.BG_SECONDARY);
        drawBorder(ctx, x, y, w, h, 1, SkyOSPalette.BORDER_SUBTLE);
        if (progress > 0f) {
            int fillW = (int) ((w - 2) * Math.min(1f, Math.max(0f, progress)));
            if (fillW > 0) {
                fillGradientH(ctx, x + 1, y + 1, fillW, h - 2,
                        SkyOSPalette.ACCENT_PRIMARY, SkyOSPalette.ACCENT_SECONDARY);
                if (fillW > 4) {
                    drawGlow(ctx, x + 1, y + 1, fillW, h - 2, SkyOSPalette.ACCENT_GLOW_SOFT, 2);
                }
            }
        }
    }

    // ─── Separator ───────────────────────────────────────────────────────────

    public void drawSeparator(DrawContext ctx, int x, int y, int w) {
        fillGradientH(ctx, x, y, w / 2, 1, 0x00000000, SkyOSPalette.BORDER_SUBTLE);
        fillGradientH(ctx, x + w / 2, y, w / 2, 1, SkyOSPalette.BORDER_SUBTLE, 0x00000000);
    }

    // ─── Screen Overlay ──────────────────────────────────────────────────────

    public void drawScreenDim(DrawContext ctx, int screenW, int screenH, int alpha) {
        fillRect(ctx, 0, 0, screenW, screenH, SkyOSPalette.withAlpha(0x000000, alpha));
    }

    public int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }
}
