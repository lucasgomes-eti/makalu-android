package eti.lucasgomes.makalu.features.orders.list

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrdersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OrdersUiState(listOf()))
    val uiState = _uiState.asStateFlow()
}