package eti.lucasgomes.makalu.features.orders.list

internal sealed interface OrdersAction {
    data object OnInitialFetch : OrdersAction
    data object OnDismissError : OrdersAction
}
