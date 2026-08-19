package eti.lucasgomes.makalu.features.orders

import eti.lucasgomes.makalu.features.orders.detail.OrderDetailViewModel
import eti.lucasgomes.makalu.features.orders.list.OrdersViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ordersModule = module {
    viewModel { params -> OrderDetailViewModel(params.get()) }
    viewModelOf(::OrdersViewModel)
    singleOf(::OrdersClient)
}
