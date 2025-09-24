package eti.lucasgomes.makalu

import eti.lucasgomes.makalu.navigation.DefaultNavigator
import eti.lucasgomes.makalu.navigation.Destination
import eti.lucasgomes.makalu.navigation.Navigator
import org.koin.dsl.module

val sharedModule = module {
    single<Navigator> {
        DefaultNavigator(startDestination = Destination.HomeGraph)
    }
}