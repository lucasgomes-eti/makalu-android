package eti.lucasgomes.makalu.features.orders.list

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class OrdersViewModel(private val ordersClient: OrdersClient) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: OrdersAction) {
        when (action) {
            OrdersAction.OnInitialFetch -> onInitialFetch()
            OrdersAction.OnDismissError -> onDismissError()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true, generalError = UiText.Empty) }
        ordersClient.getOrders().onError { error ->
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    generalError = UiText.PlainText(error.formatedMessage)
                )
            }
        }.onSuccess { response ->
            _uiState.update { state ->
                state.copy(isLoading = false, orders = response.toUiItems())
            }
        }
    }

    private fun onDismissError() = withViewModelScope {
        _uiState.update { state -> state.copy(generalError = UiText.Empty) }
    }
}
