package eti.lucasgomes.adress.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.adress.model.AddressRequest
import eti.lucasgomes.adress.ui.model.AddressAction
import eti.lucasgomes.adress.ui.model.AddressUiState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.adress.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddressViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    fun onAction(action: AddressAction) {
        when (action) {
            is AddressAction.ZipCodeChanged -> onZipCodeChanged(action.text)
            is AddressAction.StreetChanged -> onStreetChanged(action.text)
            is AddressAction.NumberChanged -> onNumberChanged(action.text)
            is AddressAction.ComplementChanged -> onComplementChanged(action.text)
            AddressAction.SaveAddressClicked -> onSaveAddressClicked()
        }
    }

    private fun onZipCodeChanged(text: String) = withViewModelScope {
        if (text.length > MAX_ZIP_CODE_LENGTH)
            return@withViewModelScope
        _uiState.update { state ->
            state.copy(
                zipCode = state.zipCode.copy(
                    text = text,
                    error = UiText.Empty
                )
            )
        }
    }

    private fun onStreetChanged(text: String) = withViewModelScope {
        _uiState.update { state ->
            state.copy(
                street = state.street.copy(
                    text = text,
                    error = UiText.Empty
                )
            )
        }
    }

    private fun onNumberChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(number = state.number.copy(text = text)) }
    }

    private fun onComplementChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(complement = state.complement.copy(text = text)) }
    }

    private fun onSaveAddressClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        withValidUiState { }
    }

    private suspend fun withValidUiState(block: suspend (AddressRequest) -> Unit) {
        val state = uiState.value
        val isStateValid = isZipCodeValid(state.zipCode.text) and isStreetValid(state.street.text)

        if (isStateValid) {
            block(buildAddressRequest(state))
        } else {
            _uiState.update { state -> state.copy(isLoading = false) }
        }
    }

    private fun isZipCodeValid(zipCode: String): Boolean {
        if (zipCode.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    zipCode = state.zipCode.copy(error = UiText.StringResource(R.string.zip_code_is_required))
                )
            }
            return false
        }
        return true
    }

    private fun isStreetValid(street: String): Boolean {
        if (street.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    street = state.street.copy(error = UiText.StringResource(R.string.street_name_is_required))
                )
            }
            return false
        }
        return true
    }

    private fun buildAddressRequest(state: AddressUiState): AddressRequest = state.run {
        AddressRequest(
            zipCode = zipCode.text,
            street = street.text,
            number = number.text,
            complement = complement.text,
            longitude = .0,
            latitude = .0
        )
    }

    companion object {
        private const val MAX_ZIP_CODE_LENGTH = 8
    }
}
