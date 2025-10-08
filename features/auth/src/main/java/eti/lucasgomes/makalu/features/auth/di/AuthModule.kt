package eti.lucasgomes.makalu.features.auth.di

import eti.lucasgomes.makalu.features.auth.login.ui.LoginViewModel
import eti.lucasgomes.makalu.features.auth.registration.ui.RegistrationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
}