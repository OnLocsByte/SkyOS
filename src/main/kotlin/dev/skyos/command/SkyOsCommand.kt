package dev.skyos.command

import dev.skyos.gui.SkyOsScreen
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.Minecraft

object SkyOsCommand {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("skyos").executes {
                    val mc = Minecraft.getInstance()
                    mc.execute { mc.setScreen(SkyOsScreen(mc.screen)) }
                    1
                }
            )
        }
    }
}
