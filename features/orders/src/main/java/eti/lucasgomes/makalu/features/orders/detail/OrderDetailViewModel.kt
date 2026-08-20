package eti.lucasgomes.makalu.features.orders.detail

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class OrderDetailViewModel(
    private val id: Long,
    private val ordersClient: OrdersClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState(id))
    val uiState = _uiState.asStateFlow()

    fun onAction(action: OrderDetailAction) {
        when (action) {
            OrderDetailAction.OnInitialFetch -> onInitialFetch()
            OrderDetailAction.OnDismissError -> onDismissError()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true, generalError = UiText.Empty) }
        ordersClient.getOrder(id).onError { error ->
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    generalError = UiText.PlainText(error.formatedMessage)
                )
            }
        }.onSuccess { response ->
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    card = response.toCardUiState(),
                    items = response.toUiItems()
                )
            }
        }
    }

    private fun onDismissError() = withViewModelScope {
        _uiState.update { state -> state.copy(generalError = UiText.Empty) }
    }
}
