package eti.lucasgomes.adress.ui.model

sealed interface AddressAction {
    data class StreetChanged(val text: String) : AddressAction
}