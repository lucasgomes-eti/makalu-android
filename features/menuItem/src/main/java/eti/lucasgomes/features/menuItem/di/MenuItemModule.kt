package eti.lucasgomes.features.menuItem.di

import eti.lucasgomes.features.menuItem.MenuItemClient
import eti.lucasgomes.features.menuItem.ui.MenuItemViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val menuItemModule = module {
    viewModel { params ->
        MenuItemViewModel(
            storeId = params[0],
            menuItemId = params[1],
            navigator = get(),
            menuItemClient = get()
        )
    }
    singleOf(::MenuItemClient)
}