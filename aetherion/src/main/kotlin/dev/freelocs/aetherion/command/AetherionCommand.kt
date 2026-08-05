package dev.freelocs.aetherion.command

import dev.freelocs.aetherion.gui.AetherionScreen
import dev.freelocs.aetherion.gui.HudEditorScreen
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.Minecraft

object AetherionCommand {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("aetherion")
                    .executes {
                        val mc = Minecraft.getInstance()
                        mc.execute { mc.setScreen(AetherionScreen(mc.screen)) }
                        1
                    }
                    .then(
                        literal("hud").executes {
                            val mc = Minecraft.getInstance()
                            mc.execute { mc.setScreen(HudEditorScreen(mc.screen, null)) }
                            1
                        }
                    )
            )
        }
    }
}
