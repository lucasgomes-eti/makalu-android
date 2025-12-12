package eti.lucasgomes.makalu.shared.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class DefaultNavigator(override val startDestination: Destination) : Navigator {
    private val _navigationActions = Channel<NavigationAction>()
    override val navigationActions = _navigationActions.receiveAsFlow()

    private val navigationResults = Channel<Any?>()

    override suspend fun navigate(
        destination: Destination,
        navOptions: NavOptions?
    ) {
        _navigationActions.send(NavigationAction.Navigate(destination, navOptions))
    }

    override suspend fun <T> navigateForResult(
        destination: Destination,
        requestKey: RequestKey
    ): T {
        _navigationActions.send(NavigationAction.NavigateForResult(destination, requestKey) {
            navigationResults.send(it)
        })

        return navigationResults.receive() as T
    }

    override suspend fun navigateUp() {
        _navigationActions.send(NavigationAction.NavigateUp)
    }

    override suspend fun <T> navigateUpWithResult(requestKey: RequestKey, result: T) {
        _navigationActions.send(NavigationAction.NavigateUpWithResult(requestKey, result))
    }
}