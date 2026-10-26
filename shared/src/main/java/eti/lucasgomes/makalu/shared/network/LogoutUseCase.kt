package eti.lucasgomes.makalu.shared.network

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class LogoutUseCase(private val dataStore: DataStore<Settings>, private val navigator: Navigator) {
    suspend operator fun invoke() {
        withContext(NonCancellable) {
            var wasLoggedIn = false
            dataStore.updateData { settings ->
                wasLoggedIn = settings.accessToken != null || settings.refreshToken != null
                settings.copy(
                    accessToken = null,
                    refreshToken = null,
                    addressId = null,
                    addressName = null
                )
            }
            if (wasLoggedIn) {
                navigator.navigate(
                    Destination.Graph.Auth,
                    NavOptions(
                        popUpTo = PopUpToOptions(
                            Destination.Graph.Root,
                            inclusive = true
                        )
                    )
                )
            }
        }
    }
}
