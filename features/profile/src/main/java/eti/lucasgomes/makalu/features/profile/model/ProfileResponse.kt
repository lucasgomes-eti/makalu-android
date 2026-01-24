package eti.lucasgomes.makalu.features.profile.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: Long,
    val name: String,
    val email: String,
    @SerialName("phone_number")
    val phoneNumber: String,
)