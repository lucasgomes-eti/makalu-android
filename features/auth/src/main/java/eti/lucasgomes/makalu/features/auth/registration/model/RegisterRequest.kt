package eti.lucasgomes.makalu.features.auth.registration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    val password: String
)
