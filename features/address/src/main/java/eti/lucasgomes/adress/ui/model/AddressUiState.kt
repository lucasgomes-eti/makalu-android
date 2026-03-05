package eti.lucasgomes.adress.ui.model

data class AddressUiState(
    val isLoading: Boolean = false,
    val zipCode: String = "",
    val street: String = "",
    val number: String? = null,
    val complement: String? = null,
    val longitude: Double = .0,
    val latitude: Double = .0
)