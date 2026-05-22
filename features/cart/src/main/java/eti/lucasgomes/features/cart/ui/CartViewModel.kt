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

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun onAction(action: CartAction) {
        when (action) {
            CartAction.OnInitialFetch -> onInitialFetch()
            is CartAction.RemoveItemClicked -> onRemoveItemClicked(action.cartItemId)
            CartAction.DeleteAllItemsClicked -> onDeleteAllItemsClicked()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { CartUiState.Loading }
        cartClient.getByStore(storeId).onError { error ->
            _uiState.update {
                CartUiState.Error(generalError = UiText.PlainText(error.formatedMessage))
            }
        }.onSuccess { response ->
            if (response.items.isEmpty()) {
                _uiState.update { CartUiState.Empty }
                return@onSuccess
            }
            _uiState.update {
                CartUiState.Data(
                    items = response.items.map { itemResponse ->
                        CartItemUiState(
                            id = itemResponse.id,
                            imageId = itemResponse.imageId,
                            name = itemResponse.name,
                            price = itemResponse.price,
                            notes = itemResponse.notes,
                            configurations = itemResponse.configurations.map { configurationResponse ->
                                CartItemUiState.Configuration(
                                    configurationResponse.configurationId,
                                    configurationResponse.name,
                                    configurationResponse.options.map { optionResponse ->
                                        CartItemUiState.Configuration.Option(
                                            optionResponse.id,
                                            optionResponse.name,
                                            optionResponse.additionalPrice,
                                            optionResponse.quantity
                                        )
                                    }
                                )
                            }
                        )
                    },
                    address = response.deliveryAddressLine?.let { text -> UiText.PlainText(text) }
                        ?: UiText.PlainText("Address not found"),
                    deliveryFee = response.total.deliveryFee,
                    totalPrice = response.total.total
                )
            }
        }
    }

    private fun onRemoveItemClicked(cartItemId: Long) = withViewModelScope {
        setItemRemoving(cartItemId, true)
        cartClient.deleteByItem(storeId, cartItemId).onError { error ->
            setItemRemoving(cartItemId, false)
            _uiState.update {
                CartUiState.Error(generalError = UiText.PlainText(error.formatedMessage))
            }
        }.onSuccess { response ->
            _uiState.update { state ->
                if (state is CartUiState.Data) {
                    if (response.items.isEmpty()) {
                        return@update CartUiState.Empty
                    }
                    val index = state.items.indexOfFirst { it.id == cartItemId }.takeIf { it != -1 }
                        ?: return@update state
                    state.copy(
                        items = state.items.toMutableList().apply { removeAt(index) },
                        totalPrice = response.total.total
                    )
                } else state
            }
        }
    }

    private fun setItemRemoving(cartItemId: Long, isRemoving: Boolean) {
        _uiState.update { state ->
            if (state is CartUiState.Data) {
                state.copy(items = state.items.map { item ->
                    if (item.id == cartItemId) {
                        item.copy(isRemoving = isRemoving)
                    } else {
                        item
                    }
                })
            } else state
        }
    }

    private fun onDeleteAllItemsClicked() = withViewModelScope {
        if (uiState.value !is CartUiState.Data) return@withViewModelScope
        _uiState.update { CartUiState.Loading }
        cartClient.deleteAllItemsByStore(storeId).onError { error ->
            _uiState.update {
                CartUiState.Error(generalError = UiText.PlainText(error.formatedMessage))
            }
        }.onSuccess {
            _uiState.update { CartUiState.Empty }
        }
    }
}