package dev.skyos.gui

import dev.skyos.config.ConfigManager
import dev.skyos.gui.SkyOsColors.BRAND_GRADIENT
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

private sealed class SidebarEntry {
    data class MainCat(val index: Int) : SidebarEntry()
    data class SubCat(val catIndex: Int, val subIndex: Int) : SidebarEntry()
}

class SkyOsScreen(private val parent: Screen?) : Screen(Component.literal("SkyOS")) {

    // ── Layout constants ──────────────────────────────────────────────────────
    private val SIDEBAR_W   = 160
    private val HEADER_H    = 52
    private val GRADIENT_H  = 3
    private val ITEM_H      = 22
    private val OPTION_H    = 44
    private val PADDING     = 12
    private val TOGGLE_W    = 34
    private val TOGGLE_H    = 14
    private val KNOB_PAD    = 2
    private val SUB_INDENT  = 20  // left indent for sub-category items

    private val LOGO = Identifier.fromNamespaceAndPath("skyos", "icon.png")

    // ── State ─────────────────────────────────────────────────────────────────
    private var selectedCategory    = 0
    private var selectedSubCategory = 0
    private var hoverEntry: SidebarEntry? = null
    private var contentScrollOffset = 0
    private var sidebarScrollOffset = 0

    // Panel geometry (computed in init)
    private var panelX = 0
    private var panelY = 0
    private var panelW = 0
    private var panelH = 0

    // ── Category / Option definitions ─────────────────────────────────────────

    private data class ToggleOption(
        val label: String,
        val description: String,
        val getter: () -> Boolean,
        val setter: (Boolean) -> Unit
    )

    private data class ConfigCategory(
        val name: String,
        val subCategories: List<String> = emptyList(),
        val optionsProvider: (subIndex: Int) -> List<ToggleOption> = { emptyList() }
    )

    private val MAIN_SUBCATEGORIES = listOf(
        "Combat", "Farming", "Fishing", "Mining",
        "Foraging", "Enchanting", "Hunting", "Slayer",
        "Dungeons", "Kuudra", "Rift", "Events"
    )

    private val categories = listOf(
        ConfigCategory("General", optionsProvider = { _ ->
            val cfg = ConfigManager.config.general
            listOf(
                ToggleOption(
                    "Firmament Announcer Remover",
                    "Remove Firmament announcement messages from chat",
                    { cfg.firmamentAnnouncerRemoverEnabled },
                    { cfg.firmamentAnnouncerRemoverEnabled = it; ConfigManager.save() }
                )
            )
        }),
        ConfigCategory("GUI"),
        ConfigCategory("Visuals"),
        ConfigCategory("Main", MAIN_SUBCATEGORIES),
        ConfigCategory("Chat"),
        ConfigCategory("Misc"),
        ConfigCategory("Dev", optionsProvider = { _ ->
            val cfg = ConfigManager.config.dev
            listOf(
                ToggleOption(
                    "Debug Mode",
                    "Show debug information in-game",
                    { cfg.debugMode },
                    { cfg.debugMode = it; ConfigManager.save() }
                )
            )
        })
    )

    // ── Sidebar flat list ─────────────────────────────────────────────────────

    private fun buildSidebarEntries(): List<SidebarEntry> {
        val result = mutableListOf<SidebarEntry>()
        categories.forEachIndexed { i, cat ->
            result.add(SidebarEntry.MainCat(i))
            if (i == selectedCategory && cat.subCategories.isNotEmpty()) {
                cat.subCategories.indices.forEach { j ->
                    result.add(SidebarEntry.SubCat(i, j))
                }
            }
        }
        return result
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun init() {
        panelW = (width * 0.56).toInt().coerceIn(480, 900)
        panelH = (height * 0.80).toInt().coerceIn(340, 700)
        panelX = (width  - panelW) / 2
        panelY = (height - panelH) / 2
    }

    override fun isPauseScreen() = false

    override fun shouldBlurBackground() = true

    override fun onClose() {
        minecraft!!.setScreen(parent)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        if (keyEvent.key == GLFW.GLFW_KEY_ESCAPE) {
            onClose()
            return true
        }
        return super.keyPressed(keyEvent)
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    override fun renderBackground(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        context.fill(0, 0, width, height, SkyOsColors.BG_OVERLAY)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        renderBackground(context, mouseX, mouseY, deltaTicks)
        renderPanel(context, mouseX, mouseY)
        super.render(context, mouseX, mouseY, deltaTicks)
    }

    private fun renderPanel(ctx: GuiGraphics, mx: Int, my: Int) {
        val cat = categories[selectedCategory]

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, SkyOsColors.BG_PANEL)

        // Brand gradient bar
        val segW = panelW.toFloat() / BRAND_GRADIENT.size
        BRAND_GRADIENT.forEachIndexed { i, color ->
            val x1 = panelX + (i * segW).toInt()
            val x2 = panelX + ((i + 1) * segW).toInt()
            ctx.fill(x1, panelY, x2, panelY + GRADIENT_H, color)
        }

        drawBorder(ctx, panelX, panelY, panelW, panelH, SkyOsColors.BORDER)

        ctx.fill(panelX, panelY + GRADIENT_H, panelX + SIDEBAR_W, panelY + panelH, SkyOsColors.BG_SIDEBAR)
        ctx.fill(panelX + SIDEBAR_W, panelY + GRADIENT_H, panelX + SIDEBAR_W + 1, panelY + panelH, SkyOsColors.BORDER)

        renderHeader(ctx)
        renderSidebar(ctx, mx, my)
        renderContent(ctx, cat, mx, my)
    }

    private fun renderHeader(ctx: GuiGraphics) {
        val hx = panelX
        val hy = panelY + GRADIENT_H
        val logoSize = 28
        val logoY = hy + (HEADER_H - logoSize) / 2
        ctx.blit(RenderPipelines.GUI_TEXTURED, LOGO, hx + PADDING, logoY, 0f, 0f, logoSize, logoSize, logoSize, logoSize, -1)
        val titleX = hx + PADDING + logoSize + 8
        val titleY = hy + (HEADER_H - 8) / 2
        ctx.drawString(font, "§fSky§bOS", titleX, titleY, SkyOsColors.FG_PRIMARY, false)
        ctx.drawString(font, "§7v0.1.0", titleX, titleY + 10, SkyOsColors.FG_DISABLED, false)
        ctx.fill(panelX, panelY + GRADIENT_H + HEADER_H, panelX + SIDEBAR_W, panelY + GRADIENT_H + HEADER_H + 1, SkyOsColors.BORDER)
    }

    private fun renderSidebar(ctx: GuiGraphics, mx: Int, my: Int) {
        val sx = panelX
        val sy = panelY + GRADIENT_H + HEADER_H + 4
        val entries = buildSidebarEntries()

        ctx.enableScissor(panelX, sy, panelX + SIDEBAR_W, panelY + panelH)

        entries.forEachIndexed { idx, entry ->
            val itemY = sy + idx * ITEM_H - sidebarScrollOffset
            if (itemY + ITEM_H < sy || itemY > panelY + panelH) return@forEachIndexed

            when (entry) {
                is SidebarEntry.MainCat -> {
                    val cat = categories[entry.index]
                    val isSelected = entry.index == selectedCategory
                    val isHovered  = hoverEntry == entry

                    when {
                        isSelected -> ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, SkyOsColors.SIDEBAR_ACTIVE)
                        isHovered  -> ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, SkyOsColors.SIDEBAR_HOVER)
                    }
                    if (isSelected) {
                        val accent = SkyOsColors.CATEGORY_ACCENTS[cat.name] ?: SkyOsColors.BRAND_PURPLE
                        ctx.fill(sx, itemY, sx + 2, itemY + ITEM_H, accent)
                    }

                    val labelColor = if (isSelected) SkyOsColors.FG_PRIMARY else SkyOsColors.FG_SECONDARY
                    ctx.drawString(font, cat.name, sx + PADDING, itemY + (ITEM_H - 8) / 2, labelColor, false)

                    if (cat.subCategories.isNotEmpty()) {
                        val arrow = if (isSelected) "▾" else "▸"
                        val arrowX = sx + SIDEBAR_W - PADDING - font.width(arrow)
                        ctx.drawString(font, arrow, arrowX, itemY + (ITEM_H - 8) / 2,
                            if (isSelected) SkyOsColors.FG_SECONDARY else SkyOsColors.FG_DISABLED, false)
                    }
                }

                is SidebarEntry.SubCat -> {
                    val cat = categories[entry.catIndex]
                    val subName = cat.subCategories[entry.subIndex]
                    val isSelected = entry.subIndex == selectedSubCategory
                    val isHovered  = hoverEntry == entry

                    when {
                        isSelected -> ctx.fill(sx + SUB_INDENT - 4, itemY, sx + SIDEBAR_W, itemY + ITEM_H, SkyOsColors.SIDEBAR_ACTIVE)
                        isHovered  -> ctx.fill(sx + SUB_INDENT - 4, itemY, sx + SIDEBAR_W, itemY + ITEM_H, SkyOsColors.SIDEBAR_HOVER)
                    }
                    if (isSelected) {
                        val accent = SkyOsColors.CATEGORY_ACCENTS[cat.name] ?: SkyOsColors.BRAND_PURPLE
                        ctx.fill(sx + SUB_INDENT - 4, itemY, sx + SUB_INDENT - 2, itemY + ITEM_H, accent)
                    }

                    // Tree connector
                    ctx.fill(sx + 6, itemY, sx + 7, itemY + ITEM_H / 2 + 1, SkyOsColors.BORDER_HI)
                    ctx.fill(sx + 7, itemY + ITEM_H / 2, sx + SUB_INDENT - 4, itemY + ITEM_H / 2 + 1, SkyOsColors.BORDER_HI)

                    val labelColor = if (isSelected) SkyOsColors.FG_PRIMARY else SkyOsColors.FG_MUTED
                    ctx.drawString(font, subName, sx + SUB_INDENT, itemY + (ITEM_H - 8) / 2, labelColor, false)
                }
            }
        }

        ctx.disableScissor()
    }

    private fun renderContent(ctx: GuiGraphics, cat: ConfigCategory, mx: Int, my: Int) {
        val cx = panelX + SIDEBAR_W + 1
        val contentW = panelW - SIDEBAR_W - 1
        val cy = panelY + GRADIENT_H + HEADER_H + 1
        val contentH = panelH - GRADIENT_H - HEADER_H - 1

        ctx.enableScissor(cx, cy, cx + contentW, cy + contentH)

        // Title: "CategoryName › SubCategoryName" when subcategories exist
        val titleText = if (cat.subCategories.isNotEmpty()) {
            "${cat.name} › ${cat.subCategories.getOrElse(selectedSubCategory) { "" }}"
        } else {
            cat.name
        }
        val titleColor = SkyOsColors.CATEGORY_ACCENTS[cat.name] ?: SkyOsColors.FG_PRIMARY
        ctx.drawString(font, titleText, cx + PADDING, cy + PADDING - contentScrollOffset, titleColor, false)

        val options = cat.optionsProvider(selectedSubCategory)
        if (options.isEmpty()) {
            val msg = "No options configured yet."
            ctx.drawString(font, msg, cx + (contentW - font.width(msg)) / 2,
                cy + contentH / 2, SkyOsColors.FG_DISABLED, false)
        } else {
            var optY = cy + PADDING + 16 - contentScrollOffset
            options.forEach { opt ->
                renderOptionEntry(ctx, opt, cx + PADDING, optY, contentW - PADDING * 2, mx, my)
                optY += OPTION_H
            }
        }

        ctx.disableScissor()
    }

    private fun renderOptionEntry(ctx: GuiGraphics, opt: ToggleOption, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        ctx.fill(x, y, x + w, y + OPTION_H - 4, SkyOsColors.BG_CARD)
        drawBorder(ctx, x, y, w, OPTION_H - 4, SkyOsColors.BORDER)
        ctx.drawString(font, opt.label, x + PADDING, y + 8, SkyOsColors.FG_PRIMARY, false)
        ctx.drawString(font, opt.description, x + PADDING, y + 20, SkyOsColors.FG_MUTED, false)
        val tX = x + w - TOGGLE_W - PADDING
        val tY = y + (OPTION_H - 4 - TOGGLE_H) / 2
        renderToggle(ctx, tX, tY, opt.getter(), mx, my)
    }

    private fun renderToggle(ctx: GuiGraphics, x: Int, y: Int, on: Boolean, mx: Int, my: Int) {
        if (on) {
            val mid = x + TOGGLE_W / 2
            ctx.fill(x, y, mid, y + TOGGLE_H, SkyOsColors.TOGGLE_ON_L)
            ctx.fill(mid, y, x + TOGGLE_W, y + TOGGLE_H, SkyOsColors.TOGGLE_ON_R)
            val kx = x + TOGGLE_W - KNOB_PAD - (TOGGLE_H - KNOB_PAD * 2)
            ctx.fill(kx, y + KNOB_PAD, kx + (TOGGLE_H - KNOB_PAD * 2), y + TOGGLE_H - KNOB_PAD, SkyOsColors.TOGGLE_KNOB)
        } else {
            ctx.fill(x, y, x + TOGGLE_W, y + TOGGLE_H, SkyOsColors.TOGGLE_OFF)
            ctx.fill(x + KNOB_PAD, y + KNOB_PAD, x + KNOB_PAD + (TOGGLE_H - KNOB_PAD * 2), y + TOGGLE_H - KNOB_PAD, SkyOsColors.TOGGLE_KNOB)
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    override fun mouseClicked(event: MouseButtonEvent, isDouble: Boolean): Boolean {
        val mx = event.x.toInt()
        val my = event.y.toInt()

        if (event.button() != 0) return super.mouseClicked(event, isDouble)

        // Sidebar click
        val sy = panelY + GRADIENT_H + HEADER_H + 4
        if (mx in panelX until panelX + SIDEBAR_W && my >= sy) {
            val entries = buildSidebarEntries()
            val relY = my - sy + sidebarScrollOffset
            val idx = relY / ITEM_H
            if (idx >= 0 && idx < entries.size) {
                when (val entry = entries[idx]) {
                    is SidebarEntry.MainCat -> {
                        selectedCategory = entry.index
                        selectedSubCategory = 0
                        contentScrollOffset = 0
                        return true
                    }
                    is SidebarEntry.SubCat -> {
                        selectedSubCategory = entry.subIndex
                        contentScrollOffset = 0
                        return true
                    }
                }
            }
        }

        // Option toggle click
        val cat = categories[selectedCategory]
        val cx = panelX + SIDEBAR_W + 1 + PADDING
        val contentW = panelW - SIDEBAR_W - 1 - PADDING * 2
        val cy = panelY + GRADIENT_H + HEADER_H + 1 + PADDING

        val options = cat.optionsProvider(selectedSubCategory)
        options.forEachIndexed { i, opt ->
            val optY = cy + 16 + i * OPTION_H - contentScrollOffset
            val tX = cx + contentW - TOGGLE_W
            val tY = optY + (OPTION_H - 4 - TOGGLE_H) / 2
            if (mx in tX until tX + TOGGLE_W && my in tY until tY + TOGGLE_H) {
                opt.setter(!opt.getter())
                return true
            }
        }

        return super.mouseClicked(event, isDouble)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        val mx = mouseX.toInt()
        val my = mouseY.toInt()
        val sy = panelY + GRADIENT_H + HEADER_H + 4

        hoverEntry = if (mx in panelX until panelX + SIDEBAR_W && my >= sy) {
            val entries = buildSidebarEntries()
            val relY = my - sy + sidebarScrollOffset
            val idx = relY / ITEM_H
            if (idx >= 0 && idx < entries.size) entries[idx] else null
        } else null
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val mx = mouseX.toInt()
        val delta = -(scrollY * 8).toInt()
        if (mx < panelX + SIDEBAR_W) {
            sidebarScrollOffset = (sidebarScrollOffset + delta).coerceAtLeast(0)
        } else {
            contentScrollOffset = (contentScrollOffset + delta).coerceAtLeast(0)
        }
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
