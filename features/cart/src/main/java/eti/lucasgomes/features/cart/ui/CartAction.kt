package eti.lucasgomes.features.cart.ui

sealed interface CartAction {
    data class RemoveItemClicked(val cartItemId: Long) : CartAction

    data object DeleteAllItemsClicked : CartAction

    data object MakeOrderClicked : CartAction
}