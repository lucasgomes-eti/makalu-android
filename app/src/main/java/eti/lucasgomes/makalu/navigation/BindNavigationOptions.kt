package eti.lucasgomes.makalu.navigation

import androidx.navigation.NavOptionsBuilder

fun bindNavigationOptions(navOptions: NavOptions?): NavOptionsBuilder.() -> Unit {
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