package eti.lucasgomes.features.store.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.features.store.ui.model.StoreAction
import eti.lucasgomes.features.store.ui.model.StoreUiState
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class StoreViewModel(private val id: Long) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState(id))
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun onAction(action: StoreAction) {
        when (action) {
            StoreAction.OnInitialFetch -> onInitialFetch()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        // TODO: fetch store
    }
}