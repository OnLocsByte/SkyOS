package dev.freelocs.aetherion.hud

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.gui.AetherionColors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * Overlay screen for repositioning HUD elements by dragging them. Opened
 * from the config GUI; every active [HudElement] is rendered at its live
 * position and can be picked up with the mouse.
 */
class HudEditorScreen(private val parent: Screen?) : Screen(Component.literal("Aetherion HUD Editor")) {

    private var draggingId: String? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0

    override fun isPauseScreen() = false

    override fun onClose() {
        ConfigManager.save()
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

        val hint = "Drag elements to reposition — Esc to close"
        context.drawString(font, hint, (width - font.width(hint)) / 2, 14, AetherionColors.FG_SECONDARY, true)

        for (element in HudManager.registered) {
            if (!element.isFeatureEnabled()) continue
            val pos = HudManager.positionOf(element.id)
            val (contentW, contentH) = element.contentSize()
            val w = (contentW * pos.scale).toInt()
            val h = (contentH * pos.scale).toInt()
            val x = (pos.xFraction * width).toInt()
            val y = (pos.yFraction * height).toInt()

            val hovered = mouseX in x..(x + w) && mouseY in y..(y + h)
            val boxColor = when {
                draggingId == element.id -> AetherionColors.BRAND_CYAN
                hovered -> AetherionColors.BRAND_INDIGO
                else -> AetherionColors.BORDER_HI
            }
            context.fill(x - 3, y - 3, x + w + 3, y + h + 3, AetherionColors.BG_CARD)
            drawBoxOutline(context, x - 3, y - 3, w + 6, h + 6, boxColor)
            context.drawString(font, element.label, x, y - 12, AetherionColors.FG_MUTED, true)

            context.pose().pushPose()
            context.pose().translate(x.toFloat(), y.toFloat(), 0f)
            context.pose().scale(pos.scale, pos.scale, 1f)
            element.render(context, 0, 0)
            context.pose().popPose()
        }

        super.render(context, mouseX, mouseY, deltaTicks)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        for (element in HudManager.registered.asReversed()) {
            if (!element.isFeatureEnabled()) continue
            val pos = HudManager.positionOf(element.id)
            val (contentW, contentH) = element.contentSize()
            val w = contentW * pos.scale
            val h = contentH * pos.scale
            val x = pos.xFraction * width
            val y = pos.yFraction * height

            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                draggingId = element.id
                dragOffsetX = (mouseX - x).toInt()
                dragOffsetY = (mouseY - y).toInt()
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        val id = draggingId ?: return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        val pos = HudManager.positionOf(id)
        val newX = (mouseX - dragOffsetX).coerceIn(0.0, width.toDouble())
        val newY = (mouseY - dragOffsetY).coerceIn(0.0, height.toDouble())
        pos.xFraction = (newX / width).toFloat()
        pos.yFraction = (newY / height).toFloat()
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (draggingId != null) {
            draggingId = null
            ConfigManager.save()
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun drawBoxOutline(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int) {
        ctx.fill(x, y, x + w, y + 1, color)
        ctx.fill(x, y + h - 1, x + w, y + h, color)
        ctx.fill(x, y, x + 1, y + h, color)
        ctx.fill(x + w - 1, y, x + w, y + h, color)
    }
}
