package eti.lucasgomes.features.checkout.ui

sealed interface CheckoutAction {
    data object OnInitialFetch : CheckoutAction
}