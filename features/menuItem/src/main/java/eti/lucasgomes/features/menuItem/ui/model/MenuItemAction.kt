package eti.lucasgomes.features.menuItem.ui.model

internal sealed interface MenuItemAction {
    data object OnInitialFetch : MenuItemAction

    data object OnDismissError : MenuItemAction

    data object NavigateBackClicked : MenuItemAction
}