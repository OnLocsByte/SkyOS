package dev.freelocs.aetherion.gui

/** A single configurable row shown in the Aetherion config content pane. */
sealed class Option {
    abstract val label: String
    abstract val description: String
}

data class ToggleOption(
    override val label: String,
    override val description: String,
    val getter: () -> Boolean,
    val setter: (Boolean) -> Unit
) : Option()

data class StepperOption(
    override val label: String,
    override val description: String,
    val getter: () -> Int,
    val setter: (Int) -> Unit,
    val min: Int,
    val max: Int,
    val step: Int = 1,
    val suffix: String = ""
) : Option()

data class ActionOption(
    override val label: String,
    override val description: String,
    val buttonText: String,
    val action: () -> Unit
) : Option()

data class TextFieldOption(
    override val label: String,
    override val description: String,
    val getter: () -> String,
    val setter: (String) -> Unit,
    val hidden: Boolean = false
) : Option()
