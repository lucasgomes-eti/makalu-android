package eti.lucasgomes.makalu.shared.navigation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class DefaultNavigator : Navigator {
    private val _navigationActions = Channel<NavigationAction>(Channel.BUFFERED)
    override val navigationActions = _navigationActions.receiveAsFlow()

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
        val result = CompletableDeferred<Any?>()
        _navigationActions.send(NavigationAction.NavigateForResult(destination, requestKey) {
            result.complete(it)
        })

        return result.await() as T
    }

    override suspend fun navigateUp() {
        _navigationActions.send(NavigationAction.NavigateUp)
    }

    override suspend fun <T> navigateUpWithResult(requestKey: RequestKey, result: T) {
        _navigationActions.send(NavigationAction.NavigateUpWithResult(requestKey, result))
    }
}