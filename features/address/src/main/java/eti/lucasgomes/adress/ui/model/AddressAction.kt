package eti.lucasgomes.adress.ui.model

sealed interface AddressAction {
    data class ZipCodeChanged(val text: String) : AddressAction

    data class StreetChanged(val text: String) : AddressAction

    data class NumberChanged(val text: String) : AddressAction

    data class ComplementChanged(val text: String) : AddressAction

    data object SaveAddressClicked : AddressAction
}