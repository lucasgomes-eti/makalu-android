package eti.lucasgomes.features.store.di

import eti.lucasgomes.features.store.StoreClient
import eti.lucasgomes.features.store.ui.StoreViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val storeModule = module {
    viewModel { params -> StoreViewModel(params.get(), get()) }
    singleOf(::StoreClient)
}