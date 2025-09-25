package eti.lucasgomes.makalu.di

import eti.lucasgomes.makalu.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
}