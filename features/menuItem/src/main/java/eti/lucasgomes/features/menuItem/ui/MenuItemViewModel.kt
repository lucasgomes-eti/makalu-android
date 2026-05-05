package eti.lucasgomes.features.menuItem.ui

import MakaluConfig
import androidx.lifecycle.ViewModel
import eti.lucasgomes.features.menuItem.MenuItemClient
import eti.lucasgomes.features.menuItem.model.MenuItemResponse
import eti.lucasgomes.features.menuItem.ui.model.ConfigurationUiState
import eti.lucasgomes.features.menuItem.ui.model.MenuItemAction
import eti.lucasgomes.features.menuItem.ui.model.MenuItemUiState
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class MenuItemViewModel(
    private val id: Long,
    private val navigator: Navigator,
    private val menuItemClient: MenuItemClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuItemUiState(id))
    val uiState = _uiState.asStateFlow()

    fun onAction(action: MenuItemAction) {
        when (action) {
            MenuItemAction.OnInitialFetch -> onInitialFetch()
            MenuItemAction.OnDismissError -> onDismissError()
            MenuItemAction.NavigateBackClicked -> onNavigateBackClicked()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state -> state.copy(isLoading = true) }
        menuItemClient.getItem(id).onError { error ->
            _uiState.update { state ->
                state.copy(
                    generalError = UiText.PlainText(error.formatedMessage),
                    isLoading = false
                )
            }
        }.onSuccess { response ->

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    imageUrl = mapMenuImageIdToUrl(response.imageId),
                    name = UiText.PlainText(response.name),
                    price = response.price,
                    ingredients = response.ingredients,
                    configurations = response.configurations.associate { config ->
                        Pair(
                            ConfigurationUiState(
                                name = config.name,
                                type = when (config.type) {
                                    MenuItemResponse.Configuration.Type.SINGLE_CHOICE -> ConfigurationUiState.Type.SINGLE_CHOICE
                                    MenuItemResponse.Configuration.Type.MULTIPLE_CHOICE -> ConfigurationUiState.Type.MULTIPLE_CHOICE
                                    MenuItemResponse.Configuration.Type.QUANTITY -> ConfigurationUiState.Type.QUANTITY
                                },
                            ), config.options
                        )
                    }
                )
            }
        }
    }

    private fun mapMenuImageIdToUrl(imageId: Long?): String? =
        imageId?.let { imageId ->
            "${MakaluConfig.BASE_URL}stores/menu/${id}/image/$imageId"
        }

    private fun onDismissError() = withViewModelScope {
        _uiState.update { state -> state.copy(generalError = UiText.Empty) }
    }

    private fun onNavigateBackClicked() = withViewModelScope {
        navigator.navigateUp()
    }
}
