package eti.lucasgomes.makalu.shared.navigation

sealed interface NavigationAction {

    data class Navigate(
        val destination: Destination,
        val navOptions: NavOptions?
    ) : NavigationAction

    data class NavigateForResult(
        val destination: Destination,
        val requestKey: RequestKey,
        val onResult: suspend (result: Any?) -> Unit
    ) : NavigationAction

    data object NavigateUp : NavigationAction

    data class NavigateUpWithResult<T>(val requestKey: RequestKey, val result: T) : NavigationAction
}