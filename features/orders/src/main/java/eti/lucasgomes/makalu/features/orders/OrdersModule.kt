package eti.lucasgomes.makalu.features.orders

import eti.lucasgomes.makalu.features.orders.detail.OrderDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ordersModule = module {
    viewModel { params -> OrderDetailViewModel(params.get()) }
}