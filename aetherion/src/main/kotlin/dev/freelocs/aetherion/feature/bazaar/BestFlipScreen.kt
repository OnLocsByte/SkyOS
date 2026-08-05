package dev.freelocs.aetherion.feature.bazaar

import dev.freelocs.aetherion.gui.AetherionColors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/** Full, scrollable list of the current best-flip results. Read-only — no buy/bid actions. */
class BestFlipScreen(private val parent: Screen?) : Screen(Component.literal("Aetherion - Best Flip")) {

    private var scroll = 0
    private var panelX = 0
    private var panelY = 0
    private var panelW = 0
    private var panelH = 0

    override fun init() {
        panelW = (width * 0.5).toInt().coerceIn(360, 700)
        panelH = (height * 0.7).toInt().coerceIn(280, 600)
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

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        ctx.fill(0, 0, width, height, AetherionColors.BG_OVERLAY)
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, AetherionColors.BG_PANEL)

        val headerY = panelY + 10
        ctx.drawString(font, "§bBest Flip §7- display only, nothing is auto-bought", panelX + 12, headerY, AetherionColors.FG_PRIMARY, false)

        val (refreshBtnX, refreshBtnW) = refreshButtonBounds()
        ctx.fill(refreshBtnX, headerY - 2, refreshBtnX + refreshBtnW, headerY + 12, AetherionColors.BG_ELEVATED)
        ctx.drawCenteredString(font, "Refresh", refreshBtnX + refreshBtnW / 2, headerY + 2, AetherionColors.FG_SECONDARY)

        val listY = panelY + 30
        val listH = panelH - 40
        ctx.enableScissor(panelX, listY, panelX + panelW, listY + listH)

        val results = BestFlipFeature.results
        if (results.isEmpty()) {
            val msg = BestFlipFeature.lastError?.let { "Error: $it" }
                ?: "No results yet - enable Best Flip in the config and wait for the first refresh."
            ctx.drawString(font, msg, panelX + 12, listY + 4 - scroll, AetherionColors.FG_MUTED, false)
        } else {
            var y = listY + 4 - scroll
            results.forEachIndexed { i, flip ->
                val sourceTag = if (flip.source == FlipResult.Source.BAZAAR) "§9[Bazaar]" else "§6[AH]"
                ctx.drawString(font, "${i + 1}. $sourceTag §f${flip.itemId}", panelX + 12, y, AetherionColors.FG_PRIMARY, false)
                ctx.drawString(
                    font,
                    "   §7buy §f${fmt(flip.buyPrice)} §7sell §f${fmt(flip.sellPrice)} §7margin §a+${fmt(flip.marginPerUnit)}",
                    panelX + 12, y + 10, AetherionColors.FG_MUTED, false
                )
                y += 24
            }
        }

        ctx.disableScissor()
        super.render(ctx, mouseX, mouseY, partialTick)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val headerY = panelY + 10
        val (refreshBtnX, refreshBtnW) = refreshButtonBounds()
        if (mouseX.toInt() in refreshBtnX..(refreshBtnX + refreshBtnW) && mouseY.toInt() in (headerY - 2)..(headerY + 12)) {
            BestFlipFeature.refreshAsync()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        scroll = (scroll - (scrollY * 12).toInt()).coerceAtLeast(0)
        return true
    }

    private fun refreshButtonBounds(): Pair<Int, Int> {
        val w = 70
        return (panelX + panelW - w - 12) to w
    }

    private fun fmt(v: Double): String = "%,.0f".format(v)
}
