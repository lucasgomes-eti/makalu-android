package eti.lucasgomes.features.menuItem.ui.model

internal data class ConfigurationUiState(
    val name: String,
    val type: Type
) {
    enum class Type { SINGLE_CHOICE, MULTIPLE_CHOICE, QUANTITY }
}
