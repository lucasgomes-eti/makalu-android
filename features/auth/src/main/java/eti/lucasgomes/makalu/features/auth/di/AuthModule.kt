package eti.lucasgomes.makalu.features.auth.di

import eti.lucasgomes.makalu.features.auth.login.LoginViewModel
import eti.lucasgomes.makalu.features.auth.registration.RegistrationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
}