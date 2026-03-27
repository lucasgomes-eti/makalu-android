package eti.lucasgomes.makalu.features.home.ui.model

internal sealed interface HomeAction {
    data object InitialFetch : HomeAction
    data object RefreshStores : HomeAction
    data class StoreClicked(val id: Long) : HomeAction
    data object AuthDialogDismissed : HomeAction
    data object AuthClicked : HomeAction
    data class CategoryClicked(val index: Int) : HomeAction

    data object AddressClicked : HomeAction
}