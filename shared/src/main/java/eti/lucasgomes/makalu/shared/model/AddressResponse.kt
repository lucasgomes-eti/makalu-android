package eti.lucasgomes.makalu.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressResponse(
    val id: Long,

    @SerialName("zip_code")
    val zipCode: String,

    val street: String,
    val number: String?,
    val complement: String?,
    val longitude: Double,
    val latitude: Double
)