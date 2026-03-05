package eti.lucasgomes.adress.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressRequest(
    @SerialName("zip_code")
    val zipCode: String,
    val street: String,
    val number: String?,
    val complement: String?,
    val longitude: Double,
    val latitude: Double
)