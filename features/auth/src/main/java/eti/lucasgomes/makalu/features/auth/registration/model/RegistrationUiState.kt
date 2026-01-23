package eti.lucasgomes.makalu.features.auth.registration.model

import android.net.Uri
import eti.lucasgomes.makalu.components.dsl.TextFieldErrorsAssignable
import eti.lucasgomes.makalu.components.dsl.TextFieldState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withFieldErrorsAsMap
import eti.lucasgomes.makalu.shared.network.MakaluError

data class RegistrationUiState(
    val profileImage: Uri? = null,
    val isImagePickerVisible: Boolean = false,
    val isPermissionDeniedDialogVisible: Boolean = false,
    val name: TextFieldState = TextFieldState(),
    val email: TextFieldState = TextFieldState(),
    val phoneNumber: TextFieldState = TextFieldState(),
    val password: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val passwordConfirmation: TextFieldState = TextFieldState(),
    val isLoading: Boolean = false,
    val generalError: String = "",
) : TextFieldErrorsAssignable {
    val isProfileImageLoaded get() = profileImage != null

    override fun assignFieldErrors(fieldErrors: List<MakaluError.FieldError>): RegistrationUiState {
        return fieldErrors.withFieldErrorsAsMap {
            copy(
                name = name.copy(error = getOrElse(::name.name) { UiText.Empty }),
                email = email.copy(error = getOrElse(::email.name) { UiText.Empty }),
                phoneNumber = phoneNumber.copy(error = getOrElse(::phoneNumber.name) { UiText.Empty }),
                password = password.copy(error = getOrElse(::password.name) { UiText.Empty }),
            )
        }
    }
}
