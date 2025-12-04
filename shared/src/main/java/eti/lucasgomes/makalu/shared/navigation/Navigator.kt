package eti.lucasgomes.makalu.shared.navigation

import kotlinx.coroutines.flow.Flow

interface Navigator {
    val startDestination: Destination
    val navigationActions: Flow<NavigationAction>

    suspend fun navigate(
        destination: Destination,
        navOptions: NavOptions? = null
    )

    suspend fun <T> navigateForResult(
        destination: Destination,
        requestKey: String
    ): T

    suspend fun navigateUp()

    suspend fun <T> navigateUpWithResult(requestKey: String, result: T)
}