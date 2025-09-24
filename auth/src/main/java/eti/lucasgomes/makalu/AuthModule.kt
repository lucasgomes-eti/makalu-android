package eti.lucasgomes.makalu

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
}