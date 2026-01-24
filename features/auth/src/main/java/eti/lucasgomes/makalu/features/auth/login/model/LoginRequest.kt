package eti.lucasgomes.makalu.features.auth.login.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)