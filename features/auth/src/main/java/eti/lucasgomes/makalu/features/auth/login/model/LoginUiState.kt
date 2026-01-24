package eti.lucasgomes.makalu.features.auth.login.model

import eti.lucasgomes.makalu.components.dsl.TextFieldErrorsAssignable
import eti.lucasgomes.makalu.components.dsl.TextFieldState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withFieldErrorsAsMap
import eti.lucasgomes.makalu.shared.network.MakaluError

data class LoginUiState(
    val email: TextFieldState = TextFieldState(),
    val password: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: UiText = UiText.Empty
) : TextFieldErrorsAssignable {

    override fun assignFieldErrors(fieldErrors: List<MakaluError.FieldError>): TextFieldErrorsAssignable {
        return fieldErrors.withFieldErrorsAsMap {
            copy(
                email = email.copy(error = getOrElse(::email.name) { UiText.Empty }),
                password = password.copy(error = getOrElse(::password.name) { UiText.Empty })
            )
        }
    }
}
