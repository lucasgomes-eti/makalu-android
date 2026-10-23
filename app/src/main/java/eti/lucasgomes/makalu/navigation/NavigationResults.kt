package eti.lucasgomes.makalu.navigation

import androidx.navigation.NavBackStackEntry
import eti.lucasgomes.makalu.shared.navigation.RequestKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

fun <T : Any> NavBackStackEntry.navigationResults(requestKey: RequestKey): Flow<T> {
    val key = requestKey.toString()
    return savedStateHandle.getStateFlow<T?>(key, null)
        .filterNotNull()
        .onEach { savedStateHandle[key] = null }
}
