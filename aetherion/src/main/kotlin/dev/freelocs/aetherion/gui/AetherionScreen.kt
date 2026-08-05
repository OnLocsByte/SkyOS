package dev.freelocs.aetherion.gui

import dev.freelocs.aetherion.config.BestFlipConfig
import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.config.DungeonTimerConfig
import dev.freelocs.aetherion.config.SkillProgressConfig
import dev.freelocs.aetherion.config.SlayerHpConfig
import dev.freelocs.aetherion.config.TooltipConfig
import dev.freelocs.aetherion.gui.AetherionColors.BRAND_GRADIENT
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * Aetherion's dark, sidebar-driven config screen. Structurally mirrors the
 * SkyOS config GUI (gradient header, sidebar categories, card-style option
 * rows) that this mod was scaffolded from, adapted for richer option types
 * (toggle / stepper / button / text field) and per-feature HUD positioning.
 */
class AetherionScreen(private val parent: Screen?) : Screen(Component.literal("Aetherion")) {

    private val SIDEBAR_W = 150
    private val HEADER_H = 44
    private val GRADIENT_H = 3
    private val ITEM_H = 24
    private val OPTION_H = 40
    private val PADDING = 12
    private val TOGGLE_W = 34
    private val TOGGLE_H = 14
    private val KNOB_PAD = 2
    private val STEP_BTN = 16

    private var selectedCategory = 0
    private var hoverIndex = -1
    private var contentScrollOffset = 0

    private var panelX = 0
    private var panelY = 0
    private var panelW = 0
    private var panelH = 0

    private lateinit var apiKeyBox: EditBox

    private val categories: List<ConfigCategory> by lazy { buildCategories() }

    private data class ConfigCategory(val name: String, val options: () -> List<Option>)

    private fun buildCategories(): List<ConfigCategory> = listOf(
        ConfigCategory("Dungeons") { dungeonOptions() },
        ConfigCategory("Skills") { skillOptions() },
        ConfigCategory("Tooltips") { tooltipOptions() },
        ConfigCategory("Slayer") { slayerOptions() },
        ConfigCategory("Best Flip") { bestFlipOptions() }
    )

    private fun dungeonOptions(): List<Option> {
        val cfg: DungeonTimerConfig = ConfigManager.config.dungeonTimer
        return listOf(
            ToggleOption("Enabled", "Show the dungeon timer overlay", { cfg.enabled }, { cfg.enabled = it; ConfigManager.save() }),
            ToggleOption("Total Timer", "Show total elapsed dungeon time", { cfg.showTotalTimer }, { cfg.showTotalTimer = it; ConfigManager.save() }),
            ToggleOption("Phase Splits", "Show time since the last room-clear split", { cfg.showPhaseSplits }, { cfg.showPhaseSplits = it; ConfigManager.save() }),
            ActionOption("HUD Position", "Drag the overlay to a new spot", "Edit") { openHudEditor("dungeon_timer") }
        )
    }

    private fun skillOptions(): List<Option> {
        val cfg: SkillProgressConfig = ConfigManager.config.skillProgress
        return listOf(
            ToggleOption("Enabled", "Show skill XP progress overlay", { cfg.enabled }, { cfg.enabled = it; ConfigManager.save() }),
            ToggleOption("Farming", "Track Farming XP gains", { cfg.showFarming }, { cfg.showFarming = it; ConfigManager.save() }),
            ToggleOption("Mining", "Track Mining XP gains", { cfg.showMining }, { cfg.showMining = it; ConfigManager.save() }),
            ToggleOption("Foraging", "Track Foraging XP gains", { cfg.showForaging }, { cfg.showForaging = it; ConfigManager.save() }),
            ToggleOption("Fishing", "Track Fishing XP gains", { cfg.showFishing }, { cfg.showFishing = it; ConfigManager.save() }),
            ToggleOption("Combat", "Track Combat XP gains", { cfg.showCombat }, { cfg.showCombat = it; ConfigManager.save() }),
            ToggleOption("Enchanting", "Track Enchanting XP gains", { cfg.showEnchanting }, { cfg.showEnchanting = it; ConfigManager.save() }),
            ToggleOption("Alchemy", "Track Alchemy XP gains", { cfg.showAlchemy }, { cfg.showAlchemy = it; ConfigManager.save() }),
            ToggleOption("Taming", "Track Taming XP gains", { cfg.showTaming }, { cfg.showTaming = it; ConfigManager.save() }),
            StepperOption("Hide After", "Seconds of inactivity before a skill row disappears", { cfg.hideAfterSeconds }, { cfg.hideAfterSeconds = it; ConfigManager.save() }, 1, 30, 1, "s"),
            ActionOption("HUD Position", "Drag the overlay to a new spot", "Edit") { openHudEditor("skill_progress") }
        )
    }

    private fun tooltipOptions(): List<Option> {
        val cfg: TooltipConfig = ConfigManager.config.tooltip
        return listOf(
            ToggleOption("Enabled", "Improve Skyblock item tooltips", { cfg.enabled }, { cfg.enabled = it; ConfigManager.save() }),
            ToggleOption("Rarity Frame", "Add a rarity-colored divider to tooltips", { cfg.rarityColorDivider }, { cfg.rarityColorDivider = it; ConfigManager.save() }),
            ToggleOption("Show Item ID", "Append the Minecraft item id to tooltips", { cfg.showItemId }, { cfg.showItemId = it; ConfigManager.save() })
        )
    }

    private fun slayerOptions(): List<Option> {
        val cfg: SlayerHpConfig = ConfigManager.config.slayerHp
        return listOf(
            ToggleOption("Enabled", "Show slayer boss HP as a bar", { cfg.enabled }, { cfg.enabled = it; ConfigManager.save() }),
            ToggleOption("Hide Action Bar Text", "Suppress the vanilla action bar HP text", { cfg.hideVanillaActionBarText }, { cfg.hideVanillaActionBarText = it; ConfigManager.save() }),
            ToggleOption("Show Percentage", "Show HP percentage next to the bar", { cfg.showPercentage }, { cfg.showPercentage = it; ConfigManager.save() }),
            ActionOption("HUD Position", "Drag the overlay to a new spot", "Edit") { openHudEditor("slayer_hp") }
        )
    }

    private fun bestFlipOptions(): List<Option> {
        val cfg: BestFlipConfig = ConfigManager.config.bestFlip
        return listOf(
            ToggleOption("Enabled", "Show the best flip price checker", { cfg.enabled }, { cfg.enabled = it; ConfigManager.save() }),
            TextFieldOption("Hypixel API Key", "From developer.hypixel.net — stored locally only", { cfg.apiKey }, { cfg.apiKey = it; ConfigManager.save() }),
            StepperOption("Min Profit", "Minimum bazaar spread to show a flip", { cfg.minProfitPercent }, { cfg.minProfitPercent = it; ConfigManager.save() }, 1, 50, 1, "%"),
            StepperOption("Max Results", "How many flips to list", { cfg.maxResults }, { cfg.maxResults = it; ConfigManager.save() }, 1, 10, 1, ""),
            StepperOption("Refresh Interval", "How often to re-check prices", { cfg.refreshIntervalSeconds }, { cfg.refreshIntervalSeconds = it; ConfigManager.save() }, 15, 300, 15, "s"),
            ToggleOption("Auction House Heuristic", "Also flag BINs far below their session average (no enchant/reforge awareness)", { cfg.includeAuctionHouseHeuristic }, { cfg.includeAuctionHouseHeuristic = it; ConfigManager.save() }),
            ActionOption("HUD Position", "Drag the overlay to a new spot", "Edit") { openHudEditor("best_flip") }
        )
    }

    private fun openHudEditor(focusId: String) {
        minecraft?.setScreen(HudEditorScreen(this, focusId))
    }

    override fun init() {
        panelW = (width * 0.56).toInt().coerceIn(460, 820)
        panelH = (height * 0.78).toInt().coerceIn(320, 620)
        panelX = (width - panelW) / 2
        panelY = (height - panelH) / 2

        apiKeyBox = EditBox(font, 0, 0, 160, 16, Component.literal("API Key"))
        apiKeyBox.setMaxLength(128)
        apiKeyBox.value = ConfigManager.config.bestFlip.apiKey
        apiKeyBox.setResponder { ConfigManager.config.bestFlip.apiKey = it; ConfigManager.save() }
        addRenderableWidget(apiKeyBox)
    }

    override fun isPauseScreen() = false

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (apiKeyBox.isFocused && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers)
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun renderBackground(ctx: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        ctx.fill(0, 0, width, height, AetherionColors.BG_OVERLAY)
    }

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        renderBackground(ctx, mouseX, mouseY, deltaTicks)
        renderPanel(ctx, mouseX, mouseY)
        super.render(ctx, mouseX, mouseY, deltaTicks)
    }

    private fun renderPanel(ctx: GuiGraphics, mx: Int, my: Int) {
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
        renderContent(ctx, mx, my)
    }

    private fun renderHeader(ctx: GuiGraphics) {
        val hx = panelX
        val hy = panelY + GRADIENT_H
        val titleY = hy + (HEADER_H - 8) / 2 - 5
        ctx.drawString(font, "§bAether§dion", hx + PADDING, titleY, AetherionColors.FG_PRIMARY, false)
        ctx.drawString(font, "§7v0.1.0", hx + PADDING, titleY + 10, AetherionColors.FG_DISABLED, false)
        ctx.fill(panelX, panelY + GRADIENT_H + HEADER_H, panelX + SIDEBAR_W, panelY + GRADIENT_H + HEADER_H + 1, AetherionColors.BORDER)
    }

    private fun renderSidebar(ctx: GuiGraphics, mx: Int, my: Int) {
        val sx = panelX
        val sy = panelY + GRADIENT_H + HEADER_H + 4

        hoverIndex = if (mx in sx until sx + SIDEBAR_W && my >= sy) {
            val idx = (my - sy) / ITEM_H
            if (idx in categories.indices) idx else -1
        } else -1

        categories.forEachIndexed { idx, cat ->
            val itemY = sy + idx * ITEM_H
            val isSelected = idx == selectedCategory
            val isHovered = idx == hoverIndex

            when {
                isSelected -> ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, AetherionColors.SIDEBAR_ACTIVE)
                isHovered -> ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, AetherionColors.SIDEBAR_HOVER)
            }
            if (isSelected) {
                val accent = AetherionColors.CATEGORY_ACCENTS[cat.name] ?: AetherionColors.BRAND_VIOLET
                ctx.fill(sx, itemY, sx + 2, itemY + ITEM_H, accent)
            }
            val labelColor = if (isSelected) AetherionColors.FG_PRIMARY else AetherionColors.FG_SECONDARY
            ctx.drawString(font, cat.name, sx + PADDING, itemY + (ITEM_H - 8) / 2, labelColor, false)
        }
    }

    private fun renderContent(ctx: GuiGraphics, mx: Int, my: Int) {
        val cat = categories[selectedCategory]
        val cx = panelX + SIDEBAR_W + 1
        val contentW = panelW - SIDEBAR_W - 1
        val cy = panelY + GRADIENT_H + HEADER_H + 1
        val contentH = panelH - GRADIENT_H - HEADER_H - 1

        ctx.enableScissor(cx, cy, cx + contentW, cy + contentH)

        val titleColor = AetherionColors.CATEGORY_ACCENTS[cat.name] ?: AetherionColors.FG_PRIMARY
        ctx.drawString(font, cat.name, cx + PADDING, cy + PADDING - contentScrollOffset, titleColor, false)

        var optY = cy + PADDING + 16 - contentScrollOffset
        val options = cat.options()
        var apiKeyBoxPositioned = false

        options.forEach { opt ->
            renderOptionEntry(ctx, opt, cx + PADDING, optY, contentW - PADDING * 2, mx, my)
            if (opt is TextFieldOption) {
                apiKeyBox.visible = true
                apiKeyBox.x = cx + contentW - PADDING - apiKeyBox.width
                apiKeyBox.y = optY + (OPTION_H - 4 - apiKeyBox.height) / 2
                apiKeyBoxPositioned = true
            }
            optY += OPTION_H
        }
        if (!apiKeyBoxPositioned) apiKeyBox.visible = false

        ctx.disableScissor()
    }

    private fun renderOptionEntry(ctx: GuiGraphics, opt: Option, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        ctx.fill(x, y, x + w, y + OPTION_H - 4, AetherionColors.BG_CARD)
        drawBorder(ctx, x, y, w, OPTION_H - 4, AetherionColors.BORDER)
        ctx.drawString(font, opt.label, x + PADDING, y + 8, AetherionColors.FG_PRIMARY, false)
        ctx.drawString(font, opt.description, x + PADDING, y + 20, AetherionColors.FG_MUTED, false)

        when (opt) {
            is ToggleOption -> {
                val tX = x + w - TOGGLE_W - PADDING
                val tY = y + (OPTION_H - 4 - TOGGLE_H) / 2
                renderToggle(ctx, tX, tY, opt.getter())
            }
            is StepperOption -> {
                val valueText = "${opt.getter()}${opt.suffix}"
                val minusX = x + w - PADDING - STEP_BTN * 2 - 30
                val plusX = x + w - PADDING - STEP_BTN
                val btnY = y + (OPTION_H - 4 - STEP_BTN) / 2
                ctx.fill(minusX, btnY, minusX + STEP_BTN, btnY + STEP_BTN, AetherionColors.BG_HOVER)
                ctx.fill(plusX, btnY, plusX + STEP_BTN, btnY + STEP_BTN, AetherionColors.BG_HOVER)
                ctx.drawCenteredString(font, "-", minusX + STEP_BTN / 2, btnY + 4, AetherionColors.FG_PRIMARY)
                ctx.drawCenteredString(font, "+", plusX + STEP_BTN / 2, btnY + 4, AetherionColors.FG_PRIMARY)
                ctx.drawCenteredString(font, valueText, minusX + STEP_BTN + (plusX - minusX - STEP_BTN) / 2, btnY + 4, AetherionColors.FG_SECONDARY)
            }
            is ActionOption -> {
                val btnW = font.width(opt.buttonText) + 16
                val btnX = x + w - PADDING - btnW
                val btnY = y + (OPTION_H - 4 - 16) / 2
                ctx.fill(btnX, btnY, btnX + btnW, btnY + 16, AetherionColors.BG_HOVER)
                drawBorder(ctx, btnX, btnY, btnW, 16, AetherionColors.BORDER_HI)
                ctx.drawCenteredString(font, opt.buttonText, btnX + btnW / 2, btnY + 4, AetherionColors.FG_PRIMARY)
            }
            is TextFieldOption -> { /* rendered via the shared EditBox widget */ }
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

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        if (button == 0) {
            val sx = panelX
            val sy = panelY + GRADIENT_H + HEADER_H + 4
            if (mx in sx until sx + SIDEBAR_W && my >= sy) {
                val idx = (my - sy) / ITEM_H
                if (idx in categories.indices) {
                    selectedCategory = idx
                    contentScrollOffset = 0
                    return true
                }
            }

            val cx = panelX + SIDEBAR_W + 1 + PADDING
            val contentW = panelW - SIDEBAR_W - 1 - PADDING * 2
            val cy = panelY + GRADIENT_H + HEADER_H + 1 + PADDING

            val options = categories[selectedCategory].options()
            options.forEachIndexed { i, opt ->
                val optY = cy + 16 + i * OPTION_H - contentScrollOffset
                when (opt) {
                    is ToggleOption -> {
                        val tX = cx + contentW - TOGGLE_W
                        val tY = optY + (OPTION_H - 4 - TOGGLE_H) / 2
                        if (mx in tX until tX + TOGGLE_W && my in tY until tY + TOGGLE_H) {
                            opt.setter(!opt.getter())
                            return true
                        }
                    }
                    is StepperOption -> {
                        val minusX = cx + contentW - STEP_BTN * 2 - 30
                        val plusX = cx + contentW - STEP_BTN
                        val btnY = optY + (OPTION_H - 4 - STEP_BTN) / 2
                        if (my in btnY until btnY + STEP_BTN) {
                            if (mx in minusX until minusX + STEP_BTN) {
                                opt.setter((opt.getter() - opt.step).coerceIn(opt.min, opt.max)); return true
                            }
                            if (mx in plusX until plusX + STEP_BTN) {
                                opt.setter((opt.getter() + opt.step).coerceIn(opt.min, opt.max)); return true
                            }
                        }
                    }
                    is ActionOption -> {
                        val btnW = font.width(opt.buttonText) + 16
                        val btnX = cx + contentW - btnW
                        val btnY = optY + (OPTION_H - 4 - 16) / 2
                        if (mx in btnX until btnX + btnW && my in btnY until btnY + 16) {
                            opt.action(); return true
                        }
                    }
                    is TextFieldOption -> {}
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        contentScrollOffset = (contentScrollOffset - (scrollY * 8).toInt()).coerceAtLeast(0)
        return true
    }

    private fun drawBorder(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int) {
        ctx.fill(x, y, x + w, y + 1, color)
        ctx.fill(x, y + h - 1, x + w, y + h, color)
        ctx.fill(x, y + 1, x + 1, y + h - 1, color)
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color)
    }
}
