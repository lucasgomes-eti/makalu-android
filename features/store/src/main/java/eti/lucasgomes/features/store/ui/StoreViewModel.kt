package eti.lucasgomes.features.store.ui

import MakaluConfig
import androidx.lifecycle.ViewModel
import eti.lucasgomes.features.store.StoreClient
import eti.lucasgomes.features.store.ui.model.MenuItemUiState
import eti.lucasgomes.features.store.ui.model.StoreAction
import eti.lucasgomes.features.store.ui.model.StoreUiState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class StoreViewModel(
    private val id: Long,
    private val client: StoreClient,
    private val navigator: Navigator
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState(id))
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    fun onAction(action: StoreAction) {
        when (action) {
            StoreAction.OnInitialFetch -> onInitialFetch()
            StoreAction.OnDismissError -> onDismissError()
            StoreAction.NavigateBackClicked -> onNavigateBackClicked()
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
                state.copy(
                    name = UiText.PlainText(response.name),
                    coverImageUrl = mapCoverImageIdToUrl(response.coverImageId),
                    deliveryFee = response.deliveryFee
                )
            }
        }

        client.getMenuItems(id).onError {
            handleGeneralError(it)
        }.onSuccess { response ->
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    menuItems = response.groupBy { it.category }.mapValues { (_, values) ->
                        values.map { itemResponse ->
                            MenuItemUiState(
                                itemResponse.id,
                                itemResponse.category,
                                itemResponse.name, itemResponse.price,
                                mapMenuImageIdToUrl(itemResponse.id, itemResponse.imageId),
                            )
                        }
                    })
            }
        }
    }

    private fun mapMenuImageIdToUrl(menuId: Long, imageId: Long?): String? =
        imageId?.let { imageId ->
            "${MakaluConfig.BASE_URL}stores/${id}/menu/${menuId}/image/$imageId"
        }

    private fun mapCoverImageIdToUrl(imageId: Long?): String? = imageId?.let { id ->
        "${MakaluConfig.BASE_URL}stores/cover-image/$id"
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

    private fun onNavigateBackClicked() = withViewModelScope {
        navigator.navigateUp()
    }
}