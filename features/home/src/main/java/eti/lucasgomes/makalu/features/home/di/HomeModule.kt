package eti.lucasgomes.makalu.features.home.di

import eti.lucasgomes.makalu.features.home.HomeClient
import eti.lucasgomes.makalu.features.home.ui.HomeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::HomeViewModel)
    singleOf(::HomeClient)
}
