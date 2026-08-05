package dev.freelocs.aetherion.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.freelocs.aetherion.gui.AetherionScreen

class AetherionModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { parent -> AetherionScreen(parent) }
}
