package eti.lucasgomes.features.cart.di

import eti.lucasgomes.features.cart.CartClient
import eti.lucasgomes.features.cart.ui.CartViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cartModule = module {
    viewModel { CartViewModel(it.get(), get(), get()) }
    singleOf(::CartClient)
}