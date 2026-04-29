package eti.lucasgomes.features.store.ui.model

internal sealed interface StoreAction {
    data object OnInitialFetch : StoreAction

    data object OnDismissError : StoreAction

    data object NavigateBackClicked : StoreAction

    data class MenuItemClicked(val id: Long) : StoreAction
}