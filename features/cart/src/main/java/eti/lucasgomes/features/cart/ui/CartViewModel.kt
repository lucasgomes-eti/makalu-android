package eti.lucasgomes.features.cart.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.features.cart.CartClient
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class CartViewModel(
    private val storeId: Long,
    private val cartClient: CartClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: CartAction) {
        when (action) {
            CartAction.OnInitialFetch -> onInitialFetch()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        cartClient.getByStore(storeId).onError { error ->
            _uiState.update { state ->
                state.copy(
                    generalError = UiText.PlainText(error.formatedMessage),
                    isLoading = false
                )
            }
        }.onSuccess { response ->
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    items = response.items.map { itemResponse ->
                        CartItemUiState(itemResponse.id)
                    }
                )
            }
        }
    }
}