package dev.freelocs.aetherion.gui

import dev.freelocs.aetherion.AetherionMod
import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.feature.bazaar.BestFlipScreen
import dev.freelocs.aetherion.gui.AetherionColors.BRAND_GRADIENT
import dev.freelocs.aetherion.gui.hud.HudEditScreen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/** Config category shown in the sidebar. One category per feature module, flat (no sub-tree). */
private data class ToggleOption(
    val label: String,
    val description: String,
    val getter: () -> Boolean,
    val setter: (Boolean) -> Unit
)

private data class StepperOption(
    val label: String,
    val description: String,
    val getter: () -> Int,
    val setter: (Int) -> Unit,
    val step: Int,
    val min: Int,
    val max: Int,
    val suffix: String = ""
)

private data class ActionOption(
    val label: String,
    val description: String,
    val onClick: (AetherionScreen) -> Unit
)

private sealed class OptionEntry(val height: Int) {
    class Toggle(val option: ToggleOption) : OptionEntry(44)
    class Stepper(val option: StepperOption) : OptionEntry(44)
    class Action(val option: ActionOption) : OptionEntry(44)
}

private data class ConfigCategory(
    val name: String,
    val optionsProvider: () -> List<OptionEntry>
)

class AetherionScreen(private val parent: Screen?) : Screen(Component.literal("Aetherion")) {

    // ── Layout constants ──────────────────────────────────────────────────────
    private val SIDEBAR_W   = 170
    private val HEADER_H    = 52
    private val GRADIENT_H  = 3
    private val ITEM_H      = 24
    private val PADDING     = 12
    private val TOGGLE_W    = 34
    private val TOGGLE_H    = 14
    private val KNOB_PAD    = 2
    private val STEP_BTN_W  = 16

    private var selectedCategory = 0
    private var hoverIndex: Int? = null
    private var contentScrollOffset = 0

    private var panelX = 0
    private var panelY = 0
    private var panelW = 0
    private var panelH = 0

    private val categories: List<ConfigCategory> by lazy { buildCategories() }

    private fun buildCategories(): List<ConfigCategory> {
        val cfg = ConfigManager.config
        return listOf(
            ConfigCategory("Dungeon Timer") {
                val c = cfg.dungeonTimer
                listOf(
                    OptionEntry.Toggle(ToggleOption("Enabled", "Show the dungeon run timer overlay",
                        { c.enabled }, { c.enabled = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Phase splits", "Track split times for boss phases / secrets",
                        { c.showPhaseSplits }, { c.showPhaseSplits = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Show deaths", "Count team deaths in the overlay",
                        { c.showDeaths }, { c.showDeaths = it; ConfigManager.save() })),
                    OptionEntry.Action(ActionOption("Move overlay", "Open the HUD editor to drag this element") {
                        it.minecraft?.setScreen(HudEditScreen(it))
                    })
                )
            },
            ConfigCategory("Skill Progress") {
                val c = cfg.skillProgress
                listOf(
                    OptionEntry.Toggle(ToggleOption("Enabled", "Show a progress bar for skill XP gained per action",
                        { c.enabled }, { c.enabled = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Show gain per action", "Display the +XP amount from the last action",
                        { c.showActionGain }, { c.showActionGain = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Show ETA to level", "Estimate time to next level from recent gain rate",
                        { c.showEtaToLevel }, { c.showEtaToLevel = it; ConfigManager.save() })),
                    OptionEntry.Stepper(StepperOption("Hide after", "Seconds of inactivity before the bar fades out",
                        { c.hideAfterSeconds }, { c.hideAfterSeconds = it; ConfigManager.save() }, 1, 1, 30, "s")),
                    OptionEntry.Action(ActionOption("Move overlay", "Open the HUD editor to drag this element") {
                        it.minecraft?.setScreen(HudEditScreen(it))
                    })
                )
            },
            ConfigCategory("Item Tooltips") {
                val c = cfg.itemRarityTooltip
                listOf(
                    OptionEntry.Toggle(ToggleOption("Enabled", "Add a rarity-colored frame line to item tooltips",
                        { c.enabled }, { c.enabled = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Colored border", "Draw a colored top/bottom rule matching item rarity",
                        { c.coloredBorder }, { c.coloredBorder = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Highlight reforges", "Emphasize the reforge name in a distinct color",
                        { c.highlightReforge }, { c.highlightReforge = it; ConfigManager.save() }))
                )
            },
            ConfigCategory("Slayer HP Bar") {
                val c = cfg.slayerHealthBar
                listOf(
                    OptionEntry.Toggle(ToggleOption("Enabled", "Replace the slayer boss action bar text with a HP bar",
                        { c.enabled }, { c.enabled = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Show percentage", "Display remaining HP as a percentage",
                        { c.showPercentage }, { c.showPercentage = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Show absolute HP", "Display raw current/max HP numbers",
                        { c.showAbsoluteHp }, { c.showAbsoluteHp = it; ConfigManager.save() })),
                    OptionEntry.Action(ActionOption("Move overlay", "Open the HUD editor to drag this element") {
                        it.minecraft?.setScreen(HudEditScreen(it))
                    })
                )
            },
            ConfigCategory("Best Flip") {
                val c = cfg.bestFlip
                listOf(
                    OptionEntry.Toggle(ToggleOption("Enabled", "Periodically check Bazaar/AH prices for flip opportunities (display only, never buys automatically)",
                        { c.enabled }, { c.enabled = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Scan Bazaar", "Include Bazaar buy/sell spread in results",
                        { c.scanBazaar }, { c.scanBazaar = it; ConfigManager.save() })),
                    OptionEntry.Toggle(ToggleOption("Scan Auction House", "Include AH lowest-BIN undercut margin in results (heavier)",
                        { c.scanAuctionHouse }, { c.scanAuctionHouse = it; ConfigManager.save() })),
                    OptionEntry.Stepper(StepperOption("Min. profit", "Minimum coins of margin to list a result",
                        { c.minProfitPerUnit }, { c.minProfitPerUnit = it; ConfigManager.save() }, 10_000, 0, 10_000_000, " coins")),
                    OptionEntry.Stepper(StepperOption("Max results", "How many flips to keep in the list",
                        { c.maxResults }, { c.maxResults = it; ConfigManager.save() }, 1, 1, 30)),
                    OptionEntry.Stepper(StepperOption("Refresh interval", "Seconds between API refreshes",
                        { c.refreshIntervalSeconds }, { c.refreshIntervalSeconds = it; ConfigManager.save() }, 30, 30, 900, "s")),
                    OptionEntry.Toggle(ToggleOption("HUD overlay", "Also show the top 3 flips as a small HUD panel",
                        { c.showHudOverlay }, { c.showHudOverlay = it; ConfigManager.save() })),
                    OptionEntry.Action(ActionOption("Open results", "View the full flip list") {
                        it.minecraft?.setScreen(BestFlipScreen(it))
                    })
                )
            },
            ConfigCategory("HUD") {
                val c = cfg.hudEditor
                listOf(
                    OptionEntry.Toggle(ToggleOption("Show labels", "Show element names while editing HUD positions",
                        { c.showLabels }, { c.showLabels = it; ConfigManager.save() })),
                    OptionEntry.Stepper(StepperOption("Snap grid", "Pixel grid HUD elements snap to while dragging",
                        { c.snapToGridPx }, { c.snapToGridPx = it; ConfigManager.save() }, 1, 1, 16, "px")),
                    OptionEntry.Action(ActionOption("Open HUD editor", "Drag every enabled overlay to a new position") {
                        it.minecraft?.setScreen(HudEditScreen(it))
                    })
                )
            }
        )
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun init() {
        panelW = (width * 0.56).toInt().coerceIn(480, 900)
        panelH = (height * 0.80).toInt().coerceIn(340, 700)
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

    // ── Rendering ─────────────────────────────────────────────────────────────

    override fun renderBackground(ctx: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        ctx.fill(0, 0, width, height, AetherionColors.BG_OVERLAY)
    }

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(ctx, mouseX, mouseY, partialTick)
        renderPanel(ctx, mouseX, mouseY)
        super.render(ctx, mouseX, mouseY, partialTick)
    }

    private fun renderPanel(ctx: GuiGraphics, mx: Int, my: Int) {
        val cat = categories[selectedCategory]

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, AetherionColors.BG_PANEL)

        val segW = panelW.toFloat() / BRAND_GRADIENT.size
        BRAND_GRADIENT.forEachIndexed { i, color ->
            val x1 = panelX + (i * segW).toInt()
            val x2 = panelX + ((i + 1) * segW).toInt()
            ctx.fill(x1, panelY, x2, panelY + GRADIENT_H, color)
        }

        drawBorder(ctx, panelX, panelY, panelW, panelH, AetherionColors.BORDER)

        ctx.fill(panelX, panelY + GRADIENT_H, panelX + SIDEBAR_W, panelY + panelH, AetherionColors.BG_SIDEBAR)
        ctx.fill(panelX + SIDEBAR_W, panelY + GRADIENT_H, panelX + SIDEBAR_W + 1, panelY + panelH, AetherionColors.BORDER)

        renderHeader(ctx)
        renderSidebar(ctx, mx, my)
        renderContent(ctx, cat, mx, my)
    }

    private fun renderHeader(ctx: GuiGraphics) {
        val hx = panelX
        val hy = panelY + GRADIENT_H
        val badgeSize = 26
        val badgeY = hy + (HEADER_H - badgeSize) / 2
        // Small gradient badge instead of an image logo.
        val segW = badgeSize.toFloat() / BRAND_GRADIENT.size
        BRAND_GRADIENT.forEachIndexed { i, color ->
            val y1 = badgeY + (i * segW).toInt()
            val y2 = badgeY + ((i + 1) * segW).toInt()
            ctx.fill(hx + PADDING, y1, hx + PADDING + badgeSize, y2, color)
        }
        drawBorder(ctx, hx + PADDING, badgeY, badgeSize, badgeSize, AetherionColors.BORDER_HI)

        val titleX = hx + PADDING + badgeSize + 8
        val titleY = hy + (HEADER_H - 8) / 2
        ctx.drawString(font, "§fAether§bion", titleX, titleY, AetherionColors.FG_PRIMARY, false)
        ctx.drawString(font, "§7v${AetherionMod.VERSION}", titleX, titleY + 10, AetherionColors.FG_DISABLED, false)
        ctx.fill(panelX, panelY + GRADIENT_H + HEADER_H, panelX + SIDEBAR_W, panelY + GRADIENT_H + HEADER_H + 1, AetherionColors.BORDER)
    }

    private fun renderSidebar(ctx: GuiGraphics, mx: Int, my: Int) {
        val sx = panelX
        val sy = panelY + GRADIENT_H + HEADER_H + 4

        ctx.enableScissor(panelX, sy, panelX + SIDEBAR_W, panelY + panelH)

        categories.forEachIndexed { idx, cat ->
            val itemY = sy + idx * ITEM_H
            if (itemY + ITEM_H < sy || itemY > panelY + panelH) return@forEachIndexed

            val isSelected = idx == selectedCategory
            val isHovered = hoverIndex == idx

            when {
                isSelected -> ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, AetherionColors.SIDEBAR_ACTIVE)
                isHovered  -> ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, AetherionColors.SIDEBAR_HOVER)
            }
            if (isSelected) {
                val accent = AetherionColors.CATEGORY_ACCENTS[cat.name] ?: AetherionColors.BRAND_VIOLET
                ctx.fill(sx, itemY, sx + 2, itemY + ITEM_H, accent)
            }

            val labelColor = if (isSelected) AetherionColors.FG_PRIMARY else AetherionColors.FG_SECONDARY
            ctx.drawString(font, cat.name, sx + PADDING, itemY + (ITEM_H - 8) / 2, labelColor, false)
        }

        ctx.disableScissor()
    }

    private fun renderContent(ctx: GuiGraphics, cat: ConfigCategory, mx: Int, my: Int) {
        val cx = panelX + SIDEBAR_W + 1
        val contentW = panelW - SIDEBAR_W - 1
        val cy = panelY + GRADIENT_H + HEADER_H + 1
        val contentH = panelH - GRADIENT_H - HEADER_H - 1

        ctx.enableScissor(cx, cy, cx + contentW, cy + contentH)

        val titleColor = AetherionColors.CATEGORY_ACCENTS[cat.name] ?: AetherionColors.FG_PRIMARY
        ctx.drawString(font, cat.name, cx + PADDING, cy + PADDING - contentScrollOffset, titleColor, false)

        var optY = cy + PADDING + 16 - contentScrollOffset
        cat.optionsProvider().forEach { entry ->
            renderOptionEntry(ctx, entry, cx + PADDING, optY, contentW - PADDING * 2, mx, my)
            optY += entry.height
        }

        ctx.disableScissor()
    }

    private fun renderOptionEntry(ctx: GuiGraphics, entry: OptionEntry, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        val h = entry.height - 4
        ctx.fill(x, y, x + w, y + h, AetherionColors.BG_CARD)
        drawBorder(ctx, x, y, w, h, AetherionColors.BORDER)

        when (entry) {
            is OptionEntry.Toggle -> {
                ctx.drawString(font, entry.option.label, x + PADDING, y + 8, AetherionColors.FG_PRIMARY, false)
                ctx.drawString(font, entry.option.description, x + PADDING, y + 20, AetherionColors.FG_MUTED, false)
                val tX = x + w - TOGGLE_W - PADDING
                val tY = y + (h - TOGGLE_H) / 2
                renderToggle(ctx, tX, tY, entry.option.getter())
            }
            is OptionEntry.Stepper -> {
                ctx.drawString(font, entry.option.label, x + PADDING, y + 8, AetherionColors.FG_PRIMARY, false)
                ctx.drawString(font, entry.option.description, x + PADDING, y + 20, AetherionColors.FG_MUTED, false)
                val valueText = "${entry.option.getter()}${entry.option.suffix}"
                val minusX = x + w - PADDING - STEP_BTN_W * 2 - font.width(valueText) - 12
                val valueX = minusX + STEP_BTN_W + 4
                val plusX = valueX + font.width(valueText) + 8
                val btnY = y + (h - 12) / 2
                ctx.fill(minusX, btnY, minusX + STEP_BTN_W, btnY + 12, AetherionColors.BG_ELEVATED)
                ctx.drawCenteredString(font, "-", minusX + STEP_BTN_W / 2, btnY + 2, AetherionColors.FG_PRIMARY)
                ctx.drawString(font, valueText, valueX, btnY + 2, AetherionColors.FG_SECONDARY, false)
                ctx.fill(plusX, btnY, plusX + STEP_BTN_W, btnY + 12, AetherionColors.BG_ELEVATED)
                ctx.drawCenteredString(font, "+", plusX + STEP_BTN_W / 2, btnY + 2, AetherionColors.FG_PRIMARY)
            }
            is OptionEntry.Action -> {
                ctx.drawString(font, entry.option.label, x + PADDING, y + 8, AetherionColors.FG_PRIMARY, false)
                ctx.drawString(font, entry.option.description, x + PADDING, y + 20, AetherionColors.FG_MUTED, false)
                val btnW = 90
                val btnX = x + w - btnW - PADDING
                val btnY = y + (h - 16) / 2
                ctx.fill(btnX, btnY, btnX + btnW, btnY + 16, AetherionColors.BG_ELEVATED)
                drawBorder(ctx, btnX, btnY, btnW, 16, AetherionColors.BORDER_HI)
                ctx.drawCenteredString(font, "Open ›", btnX + btnW / 2, btnY + 4, AetherionColors.FG_SECONDARY)
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

    // ── Input ─────────────────────────────────────────────────────────────────

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        val sy = panelY + GRADIENT_H + HEADER_H + 4
        if (mx in panelX until panelX + SIDEBAR_W && my >= sy) {
            val idx = (my - sy) / ITEM_H
            if (idx in categories.indices) {
                selectedCategory = idx
                contentScrollOffset = 0
                return true
            }
        }

        val cat = categories[selectedCategory]
        val cx = panelX + SIDEBAR_W + 1 + PADDING
        val contentW = panelW - SIDEBAR_W - 1 - PADDING * 2
        val cy = panelY + GRADIENT_H + HEADER_H + 1 + PADDING

        var optY = cy + 16 - contentScrollOffset
        cat.optionsProvider().forEach { entry ->
            val h = entry.height - 4
            if (my in optY until optY + h) {
                when (entry) {
                    is OptionEntry.Toggle -> {
                        val tX = cx + contentW - TOGGLE_W
                        val tY = optY + (h - TOGGLE_H) / 2
                        if (mx in tX until tX + TOGGLE_W && my in tY until tY + TOGGLE_H) {
                            entry.option.setter(!entry.option.getter())
                            return true
                        }
                    }
                    is OptionEntry.Stepper -> {
                        val valueText = "${entry.option.getter()}${entry.option.suffix}"
                        val minusX = cx + contentW - PADDING - STEP_BTN_W * 2 - font.width(valueText) - 12
                        val plusX = minusX + STEP_BTN_W + 4 + font.width(valueText) + 8
                        val btnY = optY + (h - 12) / 2
                        if (mx in minusX until minusX + STEP_BTN_W && my in btnY until btnY + 12) {
                            entry.option.setter((entry.option.getter() - entry.option.step).coerceIn(entry.option.min, entry.option.max))
                            return true
                        }
                        if (mx in plusX until plusX + STEP_BTN_W && my in btnY until btnY + 12) {
                            entry.option.setter((entry.option.getter() + entry.option.step).coerceIn(entry.option.min, entry.option.max))
                            return true
                        }
                    }
                    is OptionEntry.Action -> {
                        val btnW = 90
                        val btnX = cx + contentW - btnW - PADDING
                        val btnY = optY + (h - 16) / 2
                        if (mx in btnX until btnX + btnW && my in btnY until btnY + 16) {
                            entry.option.onClick(this)
                            return true
                        }
                    }
                }
            }
            optY += entry.height
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        val mx = mouseX.toInt()
        val my = mouseY.toInt()
        val sy = panelY + GRADIENT_H + HEADER_H + 4

        hoverIndex = if (mx in panelX until panelX + SIDEBAR_W && my >= sy) {
            val idx = (my - sy) / ITEM_H
            if (idx in categories.indices) idx else null
        } else null
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        contentScrollOffset = (contentScrollOffset - (scrollY * 8).toInt()).coerceAtLeast(0)
        return true
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun drawBorder(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int) {
        ctx.fill(x,         y,         x + w,     y + 1,     color)
        ctx.fill(x,         y + h - 1, x + w,     y + h,     color)
        ctx.fill(x,         y + 1,     x + 1,     y + h - 1, color)
        ctx.fill(x + w - 1, y + 1,     x + w,     y + h - 1, color)
    }
}
