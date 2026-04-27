package eti.lucasgomes.features.store.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.features.store.StoreClient
import eti.lucasgomes.features.store.ui.model.StoreAction
import eti.lucasgomes.features.store.ui.model.StoreUiState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class StoreViewModel(
    private val id: Long,
    private val client: StoreClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState(id))
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun onAction(action: StoreAction) {
        when (action) {
            StoreAction.OnInitialFetch -> onInitialFetch()
            StoreAction.OnDismissError -> onDismissError()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                name = UiText.StringResource(eti.lucasgomes.makalu.components.R.string.loading)
            )
        }
        client.getStore(id).onError {
            handleGeneralError(it)
        }.onSuccess { response ->
            _uiState.update { state ->
                state.copy(name = UiText.PlainText(response.name))
            }
        }

        client.getMenuItems(id).onError {
            handleGeneralError(it)
        }.onSuccess { response ->
            _uiState.update { state ->
                state.copy(isLoading = false, menuItems = response.groupBy { it.category })
            }
        }
    }

    private fun handleGeneralError(mkError: MakaluError) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                generalError = UiText.PlainText(mkError.formatedMessage)
            )
        }
    }

    private fun onDismissError() = withViewModelScope {
        _uiState.update { state -> state.copy(generalError = UiText.Empty) }
    }
}