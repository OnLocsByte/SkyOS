package dev.skyos.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.skyos.gui.SkyOsScreen

class SkyOsModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        ConfigScreenFactory { parent -> SkyOsScreen(parent) }
}
