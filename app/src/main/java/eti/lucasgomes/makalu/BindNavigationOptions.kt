package eti.lucasgomes.makalu

import androidx.navigation.NavOptionsBuilder
import eti.lucasgomes.makalu.navigation.NavOptions

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