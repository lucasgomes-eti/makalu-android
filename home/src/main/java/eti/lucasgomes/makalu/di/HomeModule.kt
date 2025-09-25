package eti.lucasgomes.makalu.di

import eti.lucasgomes.makalu.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::HomeViewModel)
}