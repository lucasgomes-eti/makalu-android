package eti.lucasgomes.features.checkout.di

import eti.lucasgomes.features.checkout.ui.CheckoutViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val checkoutModule = module {
    viewModelOf(::CheckoutViewModel)
}