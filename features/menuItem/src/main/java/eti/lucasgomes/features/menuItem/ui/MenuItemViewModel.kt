package eti.lucasgomes.features.menuItem.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.features.menuItem.ui.model.MenuItemAction
import eti.lucasgomes.features.menuItem.ui.model.MenuItemUiState
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class MenuItemViewModel(private val id: Long) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuItemUiState(id))
    val uiState = _uiState.asStateFlow()

    fun onAction(action: MenuItemAction) {
        when (action) {
            MenuItemAction.OnInitialFetch -> onInitialFetch()
            MenuItemAction.OnDismissError -> onDismissError()
        }
    }

    private fun onInitialFetch() = withViewModelScope { }

    private fun onDismissError() = withViewModelScope { }
}