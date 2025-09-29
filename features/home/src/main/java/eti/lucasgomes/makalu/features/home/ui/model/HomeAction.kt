package eti.lucasgomes.makalu.features.home.ui.model

internal sealed interface HomeAction {
    data object StoreClicked : HomeAction
    data object AuthDialogDismissed : HomeAction
    data object AuthClicked : HomeAction
}