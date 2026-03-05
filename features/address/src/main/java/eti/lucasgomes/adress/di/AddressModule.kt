package eti.lucasgomes.adress.di

import eti.lucasgomes.adress.ui.AddressViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val addressModule = module {
    viewModelOf(::AddressViewModel)
}