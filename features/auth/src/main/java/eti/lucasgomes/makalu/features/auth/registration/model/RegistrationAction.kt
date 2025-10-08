package eti.lucasgomes.makalu.features.auth.registration.model

sealed interface RegistrationAction {
    data class EmailChanged(val text: String) : RegistrationAction
}