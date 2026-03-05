package eti.lucasgomes.adress.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.adress.ui.model.AddressAction
import eti.lucasgomes.adress.ui.model.AddressUiState
import eti.lucasgomes.makalu.components.ext.withViewModelScope
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
        }
    }

    private fun onZipCodeChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(zipCode = text) }
    }

    private fun onStreetChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(street = text) }
    }

    private fun onNumberChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(number = text) }
    }

    private fun onComplementChanged(text: String) = withViewModelScope {
        _uiState.update { state -> state.copy(complement = text) }
    }

}