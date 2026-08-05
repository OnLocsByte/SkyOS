package dev.freelocs.aetherion.gui

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.config.HudPosition
import dev.freelocs.aetherion.hud.HudElement
import dev.freelocs.aetherion.hud.HudOverlayManager
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * Full-screen drag & drop editor for repositioning HUD overlays. Every
 * registered [HudElement] is shown as a labeled box, regardless of whether
 * its feature is currently enabled, so the user can pre-position it.
 */
class HudEditorScreen(
    private val parent: Screen?,
    private val focusId: String?
) : Screen(Component.literal("Aetherion HUD Editor")) {

    private var dragging: HudElement? = null
    private var dragGrabOffsetX = 0
    private var dragGrabOffsetY = 0
    private var hovered: HudElement? = null

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

    override fun renderBackground(ctx: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        ctx.fill(0, 0, width, height, AetherionColors.BG_OVERLAY)
    }

    override fun render(ctx: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        renderBackground(ctx, mouseX, mouseY, deltaTicks)

        val hint = "Drag boxes to reposition · Esc to close"
        ctx.drawCenteredString(font, hint, width / 2, 10, AetherionColors.FG_SECONDARY)

        hovered = null
        for (element in HudOverlayManager.all()) {
            val (x, y) = pixelPosition(element)
            val w = element.previewWidth
            val h = element.previewHeight
            val isHovered = mouseX in x..(x + w) && mouseY in y..(y + h)
            if (isHovered) hovered = element
            val isFocused = element.id == focusId

            val bg = if (element === dragging) AetherionColors.BG_HOVER else AetherionColors.BG_CARD
            ctx.fill(x, y, x + w, y + h, bg)
            val borderColor = when {
                isFocused -> AetherionColors.BRAND_VIOLET
                isHovered || element === dragging -> AetherionColors.BORDER_HI
                else -> AetherionColors.BORDER
            }
            drawBorder(ctx, x, y, w, h, borderColor)
            ctx.drawCenteredString(font, element.displayName, x + w / 2, y + h / 2 - 4, AetherionColors.FG_PRIMARY)
        }

        super.render(ctx, mouseX, mouseY, deltaTicks)
    }

    private fun pixelPosition(element: HudElement): Pair<Int, Int> {
        val pos = element.getPosition()
        return (pos.x * width).toInt() to (pos.y * height).toInt()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)
        val target = hovered ?: return super.mouseClicked(mouseX, mouseY, button)
        val (x, y) = pixelPosition(target)
        dragging = target
        dragGrabOffsetX = mouseX.toInt() - x
        dragGrabOffsetY = mouseY.toInt() - y
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        val element = dragging ?: return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)

        val newX = (mouseX.toInt() - dragGrabOffsetX)
        val newY = (mouseY.toInt() - dragGrabOffsetY)

        val maxXFrac = (1f - element.previewWidth.toFloat() / width).coerceAtLeast(0f)
        val maxYFrac = (1f - element.previewHeight.toFloat() / height).coerceAtLeast(0f)
        val xFrac = (newX.toFloat() / width).coerceIn(0f, maxXFrac)
        val yFrac = (newY.toFloat() / height).coerceIn(0f, maxYFrac)

        element.setPosition(HudPosition(xFrac, yFrac))
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (dragging != null) {
            dragging = null
            ConfigManager.save()
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun drawBorder(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int) {
        ctx.fill(x, y, x + w, y + 1, color)
        ctx.fill(x, y + h - 1, x + w, y + h, color)
        ctx.fill(x, y + 1, x + 1, y + h - 1, color)
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color)
    }
}
