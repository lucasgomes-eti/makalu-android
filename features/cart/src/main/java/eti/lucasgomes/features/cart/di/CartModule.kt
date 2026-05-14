package eti.lucasgomes.features.cart.di

import eti.lucasgomes.features.cart.ui.CartViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cartModule = module {
    viewModelOf(::CartViewModel)
}