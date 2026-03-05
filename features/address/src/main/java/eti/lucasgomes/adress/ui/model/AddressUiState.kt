package eti.lucasgomes.adress.ui.model

import eti.lucasgomes.makalu.components.dsl.TextFieldErrorsAssignable
import eti.lucasgomes.makalu.components.dsl.TextFieldState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withFieldErrorsAsMap
import eti.lucasgomes.makalu.shared.network.MakaluError

data class AddressUiState(
    val isLoading: Boolean = false,
    val zipCode: TextFieldState = TextFieldState(),
    val street: TextFieldState = TextFieldState(),
    val number: TextFieldState = TextFieldState(),
    val complement: TextFieldState = TextFieldState(),
    val longitude: Double = .0,
    val latitude: Double = .0
) : TextFieldErrorsAssignable {
    override fun assignFieldErrors(fieldErrors: List<MakaluError.FieldError>): TextFieldErrorsAssignable {
        return fieldErrors.withFieldErrorsAsMap {
            copy(
                zipCode = zipCode.copy(error = getOrElse(::zipCode.name) { UiText.Empty }),
                street = street.copy(error = getOrElse(::street.name) { UiText.Empty }),
            )
        }
    }
}