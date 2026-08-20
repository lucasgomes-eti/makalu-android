package eti.lucasgomes.makalu.features.orders.detail

internal sealed interface OrderDetailAction {
    data object OnInitialFetch : OrderDetailAction
    data object OnDismissError : OrderDetailAction
}
