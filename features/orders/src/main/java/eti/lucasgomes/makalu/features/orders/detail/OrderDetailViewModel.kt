package eti.lucasgomes.makalu.features.orders.detail

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.orders.OrdersClient
import eti.lucasgomes.makalu.features.orders.tracking.OrderTrackingRepository
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class OrderDetailViewModel(
    private val id: Long,
    private val ordersClient: OrdersClient,
    private val orderTracking: OrderTrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState(id))
    val uiState = _uiState.asStateFlow()

    init {
        onInitialFetch()
        observeLiveStatus()
    }

    fun onAction(action: OrderDetailAction) {
        when (action) {
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

    /**
     * The tracking service reports a status this screen does not show yet: load the
     * order again, quietly, so everything derived from the status is right too.
     */
    private fun observeLiveStatus() = withViewModelScope {
        orderTracking.statusOf(id).collect { status ->
            val shown = uiState.value.card?.status?.data
            if (shown != null && shown != status) refreshQuietly()
        }
    }

    private suspend fun refreshQuietly() {
        ordersClient.getOrder(id).onSuccess { response ->
            _uiState.update { state ->
                state.copy(card = response.toCardUiState(), items = response.toUiItems())
            }
        }
    }

    private fun onDismissError() = withViewModelScope {
        _uiState.update { state -> state.copy(generalError = UiText.Empty) }
    }
}
