package eti.lucasgomes.makalu.shared

import eti.lucasgomes.makalu.shared.navigation.DefaultNavigator
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import org.koin.dsl.module

val sharedModule = module {
    single<Navigator> {
        DefaultNavigator(startDestination = Destination.Graph.Auth)
    }
}