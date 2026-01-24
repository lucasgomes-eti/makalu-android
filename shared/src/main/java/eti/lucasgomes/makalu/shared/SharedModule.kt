package eti.lucasgomes.makalu.shared

import eti.lucasgomes.makalu.shared.navigation.DefaultNavigator
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.HttpClientManager
import eti.lucasgomes.makalu.shared.network.LogoutUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sharedModule = module {
    single<Navigator> {
        DefaultNavigator()
    }
    singleOf(::HttpClientManager)
    singleOf(::LogoutUseCase)
}