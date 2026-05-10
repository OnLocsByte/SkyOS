package dev.skyos.config

data class GeneralConfig(
    var firmamentAnnouncerRemoverEnabled: Boolean = true
)

data class GuiConfig(
    var placeholder: Boolean = false
)

data class VisualsConfig(
    var placeholder: Boolean = false
)

data class MainConfig(
    var placeholder: Boolean = false
)

data class ChatConfig(
    var placeholder: Boolean = false
)

data class MiscConfig(
    var placeholder: Boolean = false
)

data class DevConfig(
    var debugMode: Boolean = false
)

data class SkyOsConfig(
    var general: GeneralConfig = GeneralConfig(),
    var gui: GuiConfig = GuiConfig(),
    var visuals: VisualsConfig = VisualsConfig(),
    var main: MainConfig = MainConfig(),
    var chat: ChatConfig = ChatConfig(),
    var misc: MiscConfig = MiscConfig(),
    var dev: DevConfig = DevConfig()
)
