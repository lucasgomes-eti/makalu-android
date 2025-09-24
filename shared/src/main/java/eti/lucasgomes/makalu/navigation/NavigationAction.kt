package eti.lucasgomes.makalu.navigation

sealed interface NavigationAction {

    data class Navigate(
        val destination: Destination,
        val navOptions: NavOptions?
    ) : NavigationAction

    data object NavigateUp : NavigationAction
}