package eti.lucasgomes.features.menuItem.ui.model

internal sealed interface MenuItemAction {
    data object OnInitialFetch : MenuItemAction

    data object OnDismissError : MenuItemAction

    data object NavigateBackClicked : MenuItemAction

    data class OptionSelected(
        val configKey: ConfigurationUiState,
        val optionIndex: Int
    ) : MenuItemAction

    data class QuantityChanged(
        val configKey: ConfigurationUiState,
        val optionIndex: Int,
        val amount: Int
    ) : MenuItemAction

    data class NotesChanged(val value: String) : MenuItemAction
}