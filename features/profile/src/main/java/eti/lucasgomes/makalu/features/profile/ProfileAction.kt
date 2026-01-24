package eti.lucasgomes.makalu.features.profile

sealed interface ProfileAction {
    data object LogoutClicked : ProfileAction
    data object ErrorDismiss : ProfileAction
    data object InitialFetch : ProfileAction
}