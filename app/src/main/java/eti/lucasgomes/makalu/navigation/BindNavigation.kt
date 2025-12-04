package eti.lucasgomes.makalu.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.NavigationAction
import kotlinx.coroutines.launch

@Composable
fun bindNavigationEvents(concreteNavigator: NavHostController): (NavigationAction) -> Unit {
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    return { action ->
        when (action) {
            is NavigationAction.Navigate -> concreteNavigator.navigate(
                action.destination,
                bindNavigateOptions(action.navOptions)
            )

            is NavigationAction.NavigateForResult -> {
                concreteNavigator.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
                    savedStateHandle.getLiveData<Any?>(action.requestKey).observe(owner) {
                        scope.launch {
                            action.onResult(it)
                            savedStateHandle.remove<Any?>(action.requestKey)
                        }
                    }
                }
                concreteNavigator.navigate(action.destination)
            }

            NavigationAction.NavigateUp -> concreteNavigator.navigateUp()

            is NavigationAction.NavigateUpWithResult<*> -> {
                concreteNavigator.previousBackStackEntry?.savedStateHandle?.set(
                    action.requestKey,
                    action.result
                )
                concreteNavigator.navigateUp()
            }
        }
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
