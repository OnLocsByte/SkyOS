package dev.freelocs.aetherion.gui

import dev.freelocs.aetherion.AetherionMod
import dev.freelocs.aetherion.features.AetherionFeature
import dev.freelocs.aetherion.features.ConfigOption
import dev.freelocs.aetherion.features.FeatureRegistry
import dev.freelocs.aetherion.hud.HudEditorScreen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW

private const val HUD_CATEGORY = "HUD & Overlays"

/**
 * Dark-themed config GUI. Structurally mirrors SkyOS's config screen (same
 * sidebar/content/toggle layout) but the content is generated entirely
 * from [FeatureRegistry] instead of a hand-written option list, so a new
 * feature only has to register itself to show up here.
 */
class AetherionScreen(private val parent: Screen?) : Screen(Component.literal("Aetherion")) {

    // ── Layout constants ────────────────────────────────────────────────────
    private val SIDEBAR_W   = 150
    private val HEADER_H    = 52
    private val GRADIENT_H  = 3
    private val ITEM_H      = 22
    private val PADDING     = 12
    private val CARD_GAP    = 8
    private val ROW_H       = 20
    private val SUB_ROW_H   = 18
    private val TOGGLE_W    = 30
    private val TOGGLE_H    = 13
    private val KNOB_PAD    = 2

    private val LOGO = ResourceLocation.fromNamespaceAndPath(AetherionMod.MOD_ID, "icon.png")

    private var selectedCategory = 0
    private var contentScrollOffset = 0

    private var panelX = 0
    private var panelY = 0
    private var panelW = 0
    private var panelH = 0

    private val categories: List<String> by lazy {
        FeatureRegistry.byCategory().keys.toList() + HUD_CATEGORY
    }

    private data class ToggleHit(val x: Int, val y: Int, val w: Int, val h: Int, val onClick: () -> Unit)
    private val toggleHits = mutableListOf<ToggleHit>()

    override fun init() {
        panelW = (width * 0.62).toInt().coerceIn(500, 900)
        panelH = (height * 0.78).toInt().coerceIn(340, 680)
        panelX = (width - panelW) / 2
        panelY = (height - panelH) / 2
    }

    override fun isPauseScreen() = false

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun renderBackground(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        context.fill(0, 0, width, height, AetherionColors.BG_OVERLAY)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        renderBackground(context, mouseX, mouseY, deltaTicks)
        renderPanel(context, mouseX, mouseY)
        super.render(context, mouseX, mouseY, deltaTicks)
    }

    private fun renderPanel(ctx: GuiGraphics, mx: Int, my: Int) {
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, AetherionColors.BG_PANEL)

        val segW = panelW.toFloat() / AetherionColors.BRAND_GRADIENT.size
        AetherionColors.BRAND_GRADIENT.forEachIndexed { i, color ->
            val x1 = panelX + (i * segW).toInt()
            val x2 = panelX + ((i + 1) * segW).toInt()
            ctx.fill(x1, panelY, x2, panelY + GRADIENT_H, color)
        }

        drawBorder(ctx, panelX, panelY, panelW, panelH, AetherionColors.BORDER)

        ctx.fill(panelX, panelY + GRADIENT_H, panelX + SIDEBAR_W, panelY + panelH, AetherionColors.BG_SIDEBAR)
        ctx.fill(panelX + SIDEBAR_W, panelY + GRADIENT_H, panelX + SIDEBAR_W + 1, panelY + panelH, AetherionColors.BORDER)

        renderHeader(ctx)
        renderSidebar(ctx, mx, my)
        renderContent(ctx, mx, my)
    }

    private fun renderHeader(ctx: GuiGraphics) {
        val hx = panelX
        val hy = panelY + GRADIENT_H
        val logoSize = 26
        val logoY = hy + (HEADER_H - logoSize) / 2
        ctx.blit(LOGO, hx + PADDING, logoY, 0f, 0f, logoSize, logoSize, logoSize, logoSize)
        val titleX = hx + PADDING + logoSize + 8
        val titleY = hy + (HEADER_H - 8) / 2
        ctx.drawString(font, "§fAether§dion", titleX, titleY, AetherionColors.FG_PRIMARY, false)
        ctx.drawString(font, "§7v${AetherionMod.VERSION}", titleX, titleY + 10, AetherionColors.FG_DISABLED, false)
        ctx.fill(panelX, panelY + GRADIENT_H + HEADER_H, panelX + SIDEBAR_W, panelY + GRADIENT_H + HEADER_H + 1, AetherionColors.BORDER)
    }

    private fun renderSidebar(ctx: GuiGraphics, mx: Int, my: Int) {
        val sx = panelX
        val sy = panelY + GRADIENT_H + HEADER_H + 4

        ctx.enableScissor(panelX, sy, panelX + SIDEBAR_W, panelY + panelH)
        categories.forEachIndexed { idx, name ->
            val itemY = sy + idx * ITEM_H
            val isSelected = idx == selectedCategory
            val isHovered = mx in sx until sx + SIDEBAR_W && my in itemY until itemY + ITEM_H

            when {
                isSelected -> ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, AetherionColors.SIDEBAR_ACTIVE)
                isHovered -> ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, AetherionColors.SIDEBAR_HOVER)
            }
            if (isSelected) {
                ctx.fill(sx, itemY, sx + 2, itemY + ITEM_H, AetherionColors.BRAND_CYAN)
            }
            val labelColor = if (isSelected) AetherionColors.FG_PRIMARY else AetherionColors.FG_SECONDARY
            ctx.drawString(font, name, sx + PADDING, itemY + (ITEM_H - 8) / 2, labelColor, false)
        }
        ctx.disableScissor()
    }

    private fun renderContent(ctx: GuiGraphics, mx: Int, my: Int) {
        toggleHits.clear()

        val cx = panelX + SIDEBAR_W + 1
        val contentW = panelW - SIDEBAR_W - 1
        val cy = panelY + GRADIENT_H + HEADER_H + 1
        val contentH = panelH - GRADIENT_H - HEADER_H - 1
        val categoryName = categories[selectedCategory]

        ctx.enableScissor(cx, cy, cx + contentW, cy + contentH)
        ctx.drawString(font, categoryName, cx + PADDING, cy + PADDING - contentScrollOffset, AetherionColors.FG_PRIMARY, false)

        var y = cy + PADDING + 18 - contentScrollOffset
        if (categoryName == HUD_CATEGORY) {
            y = renderHudCategory(ctx, cx + PADDING, y, contentW - PADDING * 2, mx, my)
        } else {
            val features = FeatureRegistry.byCategory()[categoryName].orEmpty()
            for (feature in features) {
                y = renderFeatureCard(ctx, feature, cx + PADDING, y, contentW - PADDING * 2, mx, my)
                y += CARD_GAP
            }
        }

        ctx.disableScissor()
    }

    private fun renderHudCategory(ctx: GuiGraphics, x: Int, y: Int, w: Int, mx: Int, my: Int): Int {
        val desc = "Drag every active HUD overlay to wherever you want it on screen."
        ctx.drawString(font, desc, x, y, AetherionColors.FG_MUTED, false)

        val buttonY = y + 18
        val buttonW = 170
        val buttonH = 22
        val hovered = mx in x until x + buttonW && my in buttonY until buttonY + buttonH
        ctx.fill(x, buttonY, x + buttonW, buttonY + buttonH, if (hovered) AetherionColors.BG_ELEVATED else AetherionColors.BG_CARD)
        drawBorder(ctx, x, buttonY, buttonW, buttonH, if (hovered) AetherionColors.BRAND_CYAN else AetherionColors.BORDER)
        val label = "Edit HUD Positions"
        ctx.drawString(font, label, x + (buttonW - font.width(label)) / 2, buttonY + (buttonH - 8) / 2, AetherionColors.FG_PRIMARY, false)

        toggleHits += ToggleHit(x, buttonY, buttonW, buttonH) {
            minecraft?.setScreen(HudEditorScreen(this))
        }
        return buttonY + buttonH
    }

    private fun renderFeatureCard(ctx: GuiGraphics, feature: AetherionFeature, x: Int, y: Int, w: Int, mx: Int, my: Int): Int {
        val subOptions = if (feature.enabled) feature.subOptions() else emptyList()
        val cardH = ROW_H + subOptions.size * SUB_ROW_H + 6

        ctx.fill(x, y, x + w, y + cardH, AetherionColors.BG_CARD)
        drawBorder(ctx, x, y, w, cardH, AetherionColors.BORDER)

        ctx.drawString(font, feature.displayName, x + PADDING, y + 6, AetherionColors.FG_PRIMARY, false)
        val descY = y + 6 + 10
        if (subOptions.isEmpty() || !feature.enabled) {
            ctx.drawString(font, feature.description, x + PADDING, descY, AetherionColors.FG_MUTED, false)
        }

        val toggleX = x + w - TOGGLE_W - PADDING
        val toggleY = y + (ROW_H - TOGGLE_H) / 2
        renderToggle(ctx, toggleX, toggleY, feature.enabled)
        toggleHits += ToggleHit(toggleX, toggleY, TOGGLE_W, TOGGLE_H) {
            feature.enabled = !feature.enabled
        }

        var subY = y + ROW_H + 4
        for (option in subOptions) {
            renderSubOption(ctx, option, x + PADDING, subY, w - PADDING * 2)
            subY += SUB_ROW_H
        }

        return y + cardH
    }

    private fun renderSubOption(ctx: GuiGraphics, option: ConfigOption, x: Int, y: Int, w: Int) {
        when (option) {
            is ConfigOption.Toggle -> {
                ctx.drawString(font, option.label, x + 8, y + (SUB_ROW_H - 8) / 2, AetherionColors.FG_SECONDARY, false)
                val toggleX = x + w - TOGGLE_W
                val toggleY = y + (SUB_ROW_H - TOGGLE_H) / 2
                renderToggle(ctx, toggleX, toggleY, option.getter())
                toggleHits += ToggleHit(toggleX, toggleY, TOGGLE_W, TOGGLE_H) {
                    option.setter(!option.getter())
                }
            }
        }
    }

    private fun renderToggle(ctx: GuiGraphics, x: Int, y: Int, on: Boolean) {
        if (on) {
            val mid = x + TOGGLE_W / 2
            ctx.fill(x, y, mid, y + TOGGLE_H, AetherionColors.TOGGLE_ON_L)
            ctx.fill(mid, y, x + TOGGLE_W, y + TOGGLE_H, AetherionColors.TOGGLE_ON_R)
            val kx = x + TOGGLE_W - KNOB_PAD - (TOGGLE_H - KNOB_PAD * 2)
            ctx.fill(kx, y + KNOB_PAD, kx + (TOGGLE_H - KNOB_PAD * 2), y + TOGGLE_H - KNOB_PAD, AetherionColors.TOGGLE_KNOB)
        } else {
            ctx.fill(x, y, x + TOGGLE_W, y + TOGGLE_H, AetherionColors.TOGGLE_OFF)
            ctx.fill(x + KNOB_PAD, y + KNOB_PAD, x + KNOB_PAD + (TOGGLE_H - KNOB_PAD * 2), y + TOGGLE_H - KNOB_PAD, AetherionColors.TOGGLE_KNOB)
        }
    }

    // ── Input ────────────────────────────────────────────────────────────────

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)
        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        val sy = panelY + GRADIENT_H + HEADER_H + 4
        if (mx in panelX until panelX + SIDEBAR_W && my >= sy) {
            val idx = (my - sy) / ITEM_H
            if (idx in categories.indices) {
                selectedCategory = idx
                contentScrollOffset = 0
                return true
            }
        }

        for (hit in toggleHits) {
            if (mx in hit.x until hit.x + hit.w && my in hit.y until hit.y + hit.h) {
                hit.onClick()
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        contentScrollOffset = (contentScrollOffset - (scrollY * 10).toInt()).coerceAtLeast(0)
        return true
    }

    private fun drawBorder(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int) {
        ctx.fill(x, y, x + w, y + 1, color)
        ctx.fill(x, y + h - 1, x + w, y + h, color)
        ctx.fill(x, y + 1, x + 1, y + h - 1, color)
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color)
    }
}
