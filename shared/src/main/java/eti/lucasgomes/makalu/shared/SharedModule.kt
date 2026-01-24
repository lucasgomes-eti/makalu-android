package eti.lucasgomes.makalu.shared

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.shared.navigation.DefaultNavigator
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sharedModule = module {
    single<Navigator> {
        val dataStore = get<DataStore<Settings>>()
        //TODO: Check if user is logged in on SplashScreen
        val accessToken = runBlocking { dataStore.data.first().accessToken }

        DefaultNavigator(startDestination = if (accessToken != null) Destination.Graph.Home else Destination.Graph.Auth)
    }
    singleOf(::HttpClientManager)
}