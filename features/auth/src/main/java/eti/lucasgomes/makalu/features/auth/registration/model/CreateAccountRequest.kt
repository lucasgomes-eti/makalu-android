package eti.lucasgomes.makalu.features.auth.registration.model

data class CreateAccountRequest(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val password: String
)
