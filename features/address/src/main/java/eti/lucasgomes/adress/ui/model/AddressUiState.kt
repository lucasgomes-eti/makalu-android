package eti.lucasgomes.adress.ui.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
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
    val location: LatLng = LatLng(.0, .0),
    val isLocationLoading: Boolean = false,
    val locationError: UiText = UiText.Empty,
    val isPermissionDeniedDialogVisible: Boolean = false
) : TextFieldErrorsAssignable {

    val marker: MarkerState
        get() = MarkerState(position = location)

    val isLocationProvided: Boolean
        get() = location.latitude != .0

    override fun assignFieldErrors(fieldErrors: List<MakaluError.FieldError>): TextFieldErrorsAssignable {
        return fieldErrors.withFieldErrorsAsMap {
            copy(
                zipCode = zipCode.copy(error = getOrElse(::zipCode.name) { UiText.Empty }),
                street = street.copy(error = getOrElse(::street.name) { UiText.Empty }),
            )
        }
    }
}