package eti.lucasgomes.features.checkout.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class CheckoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: CheckoutAction) {
        when (action) {
            CheckoutAction.OnInitialFetch -> onInitialFetch()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state ->
            state.copy(
                items = listOf(
                    CartItemUiState(1),
                    CartItemUiState(2),
                    CartItemUiState(3),
                    CartItemUiState(4)
                )
            )
        }
    }
}