package eti.lucasgomes.adress.model

data class ReverseGeocodeResponse(
    val zipCode: String = "",
    val street: String = "",
    val number: String = "",
    val complement: String = ""
)