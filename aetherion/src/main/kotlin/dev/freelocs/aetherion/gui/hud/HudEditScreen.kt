package dev.freelocs.aetherion.gui.hud

import dev.freelocs.aetherion.config.ConfigManager
import dev.freelocs.aetherion.feature.hud.HudElement
import dev.freelocs.aetherion.feature.hud.HudManager
import dev.freelocs.aetherion.gui.AetherionColors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * Lets the player drag every registered [HudElement] to a new screen position.
 * Positions are stored per-element in the config and picked up immediately by
 * the live overlay renderer.
 */
class HudEditScreen(private val parent: Screen?) : Screen(Component.literal("Aetherion HUD Editor")) {

    private var dragging: HudElement? = null
    private var dragOffsetX = 0
    private var dragOffsetY = 0

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

        val hint = "Drag elements to reposition them  •  ESC to close"
        ctx.drawCenteredString(font, hint, width / 2, 10, AetherionColors.FG_SECONDARY)

        val showLabels = ConfigManager.config.hudEditor.showLabels
        HudManager.all().forEach { element ->
            val pos = element.getPosition()
            val (w, h) = element.previewSize()
            val boxW = (w * pos.scale).toInt().coerceAtLeast(20)
            val boxH = (h * pos.scale).toInt().coerceAtLeast(14)

            val isDragging = dragging === element
            val isHovered = !isDragging && mouseX in pos.x..(pos.x + boxW) && mouseY in pos.y..(pos.y + boxH)

            ctx.fill(pos.x, pos.y, pos.x + boxW, pos.y + boxH,
                if (isDragging) 0x552FA6FF else if (isHovered) 0x40FFFFFF else 0x30FFFFFF)
            drawBorder(ctx, pos.x, pos.y, boxW, boxH,
                if (isDragging || isHovered) AetherionColors.BRAND_SKY else AetherionColors.BORDER_HI)

            element.renderPreview(ctx, pos.x + 4, pos.y + 3)

            if (showLabels) {
                ctx.drawString(font, element.label, pos.x + 4, pos.y - 10, AetherionColors.FG_MUTED, true)
            }
        }

        super.render(ctx, mouseX, mouseY, partialTick)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        val mx = mouseX.toInt()
        val my = mouseY.toInt()
        // Topmost (last-registered) element wins on overlap.
        val hit = HudManager.all().lastOrNull { element ->
            val pos = element.getPosition()
            val (w, h) = element.previewSize()
            val boxW = (w * pos.scale).toInt().coerceAtLeast(20)
            val boxH = (h * pos.scale).toInt().coerceAtLeast(14)
            mx in pos.x..(pos.x + boxW) && my in pos.y..(pos.y + boxH)
        }
        if (hit != null) {
            dragging = hit
            val pos = hit.getPosition()
            dragOffsetX = mx - pos.x
            dragOffsetY = my - pos.y
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        val element = dragging ?: return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        val pos = element.getPosition()
        val grid = ConfigManager.config.hudEditor.snapToGridPx.coerceAtLeast(1)

        var newX = mouseX.toInt() - dragOffsetX
        var newY = mouseY.toInt() - dragOffsetY
        newX = (newX / grid) * grid
        newY = (newY / grid) * grid

        pos.x = newX.coerceIn(0, width - 10)
        pos.y = newY.coerceIn(0, height - 10)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (dragging != null) {
            dragging?.savePosition()
            dragging = null
            ConfigManager.save()
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun drawBorder(ctx: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int) {
        ctx.fill(x,         y,         x + w,     y + 1,     color)
        ctx.fill(x,         y + h - 1, x + w,     y + h,     color)
        ctx.fill(x,         y + 1,     x + 1,     y + h - 1, color)
        ctx.fill(x + w - 1, y + 1,     x + w,     y + h - 1, color)
    }
}
