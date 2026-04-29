package eti.lucasgomes.features.menuItem.di

import eti.lucasgomes.features.menuItem.ui.MenuItemViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val menuItemModule = module {
    viewModel { params -> MenuItemViewModel(params.get()) }
}