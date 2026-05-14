package eti.lucasgomes.features.cart.ui

sealed interface CartAction {
    data object OnInitialFetch : CartAction
}