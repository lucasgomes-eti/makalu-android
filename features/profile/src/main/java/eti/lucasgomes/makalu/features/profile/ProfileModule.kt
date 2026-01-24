package eti.lucasgomes.makalu.features.profile

import eti.lucasgomes.makalu.features.profile.ui.ProfileViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
    viewModelOf(::ProfileViewModel)
    singleOf(::ProfileClient)
}