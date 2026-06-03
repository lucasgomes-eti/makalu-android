package eti.lucasgomes.makalu.features.orders.detail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderDetailViewModel(private val id: Long) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState(id))
    val uiState = _uiState.asStateFlow()
}