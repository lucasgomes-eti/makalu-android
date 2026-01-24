package eti.lucasgomes.makalu.features.profile.model

sealed interface ProfileAction {
    data object LogoutClicked : ProfileAction
    data object ErrorDismiss : ProfileAction
    data object InitialFetch : ProfileAction
    data object LogoutDialogDismissed : ProfileAction
    data object LogoutConfirmed : ProfileAction
}
