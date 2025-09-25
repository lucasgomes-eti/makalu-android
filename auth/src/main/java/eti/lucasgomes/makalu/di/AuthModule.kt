package eti.lucasgomes.makalu.di

import eti.lucasgomes.makalu.login.LoginViewModel
import eti.lucasgomes.makalu.registration.RegistrationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
}