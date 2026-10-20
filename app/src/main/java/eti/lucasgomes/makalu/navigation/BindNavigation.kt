package eti.lucasgomes.makalu.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.NavigationAction

@Composable
fun bindNavigationEvents(navHostController: NavHostController): (NavigationAction) -> Unit {
    return { action ->
        when (action) {
            is NavigationAction.Navigate -> navHostController.navigate(
                action.destination,
                bindNavigateOptions(action.navOptions)
            )

            is NavigationAction.NavigateForResult -> {
                navHostController.currentBackStackEntry?.let { entry ->
                    val key = action.requestKey.toString()
                    val liveData = entry.savedStateHandle.getLiveData<Any?>(key)
                    liveData.observe(entry, object : Observer<Any?> {
                        override fun onChanged(value: Any?) {
                            liveData.removeObserver(this)
                            entry.savedStateHandle.remove<Any?>(key)
                            action.onResult(value)
                        }
                    })
                }
                navHostController.navigate(action.destination)
            }

            NavigationAction.NavigateUp -> navHostController.navigateUp()

            is NavigationAction.NavigateUpWithResult<*> -> {
                navHostController.previousBackStackEntry?.savedStateHandle?.set(
                    action.requestKey.toString(),
                    action.result
                )
                navHostController.navigateUp()
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
