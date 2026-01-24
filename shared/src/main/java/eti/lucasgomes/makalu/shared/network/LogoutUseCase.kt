package eti.lucasgomes.makalu.shared.network

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import eti.lucasgomes.makalu.shared.settings.Settings

class LogoutUseCase(
    private val dataStore: DataStore<Settings>,
    private val navigator: Navigator
) {
    suspend operator fun invoke() {
        dataStore.updateData { settings -> settings.copy(accessToken = null, refreshToken = null) }
        navigator.navigate(
            Destination.Graph.Auth, NavOptions(
                popUpTo = PopUpToOptions(
                    Destination.Graph.Auth,
                    inclusive = false
                )
            )
        )
    }
}