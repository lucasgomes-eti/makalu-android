package eti.lucasgomes.makalu.features.orders.list

internal sealed interface OrdersAction {
    data object OnDismissError : OrdersAction
    data class OnOrderClicked(val orderId: Long) : OrdersAction
}
