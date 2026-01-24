package eti.lucasgomes.makalu.shared.navigation

import kotlinx.coroutines.flow.Flow

interface Navigator {
    val navigationActions: Flow<NavigationAction>

    suspend fun navigate(
        destination: Destination,
        navOptions: NavOptions? = null
    )

    suspend fun <T> navigateForResult(
        destination: Destination,
        requestKey: RequestKey
    ): T

    suspend fun navigateUp()

    suspend fun <T> navigateUpWithResult(requestKey: RequestKey, result: T)
}