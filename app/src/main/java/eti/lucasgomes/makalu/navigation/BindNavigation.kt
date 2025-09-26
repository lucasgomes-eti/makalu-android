package eti.lucasgomes.makalu.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.NavigationAction

@Composable
fun bindNavigationEvents(concreteNavigator: NavHostController): (NavigationAction) -> Unit =
    { action ->
        when (action) {
            is NavigationAction.Navigate -> concreteNavigator.navigate(
                action.destination,
                bindNavigateOptions(action.navOptions)
            )

            NavigationAction.NavigateUp -> concreteNavigator.navigateUp()
        }
    }

private fun bindNavigateOptions(navOptions: NavOptions?): NavOptionsBuilder.() -> Unit {
    return {
        navOptions?.let { options ->
            options.popUpTo?.let { popUpToOptions ->
                popUpTo(popUpToOptions.destination) {
                    inclusive = popUpToOptions.inclusive
                    saveState = popUpToOptions.saveState
                }
            }
        }
    }
}