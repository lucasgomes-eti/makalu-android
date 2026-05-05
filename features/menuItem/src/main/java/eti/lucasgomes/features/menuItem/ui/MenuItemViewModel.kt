package eti.lucasgomes.features.menuItem.ui

import MakaluConfig
import androidx.lifecycle.ViewModel
import eti.lucasgomes.features.menuItem.MenuItemClient
import eti.lucasgomes.features.menuItem.model.MenuItemResponse
import eti.lucasgomes.features.menuItem.ui.model.ConfigurationUiState
import eti.lucasgomes.features.menuItem.ui.model.MenuItemAction
import eti.lucasgomes.features.menuItem.ui.model.MenuItemUiState
import eti.lucasgomes.features.menuItem.ui.model.OptionUiState
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
            is MenuItemAction.OptionSelected -> onOptionSelected(
                action.configKey,
                action.optionIndex
            )

            is MenuItemAction.QuantityChanged -> onQuantityChanged(
                action.configKey,
                action.optionIndex,
                action.amount
            )

            is MenuItemAction.NotesChanged -> onNotesChanged(action.value)
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
                            ), config.options.map { option ->
                                when (config.type) {
                                    MenuItemResponse.Configuration.Type.SINGLE_CHOICE -> OptionUiState.SingleChoice(
                                        option
                                    )

                                    MenuItemResponse.Configuration.Type.MULTIPLE_CHOICE -> OptionUiState.MultipleChoice(
                                        option
                                    )

                                    MenuItemResponse.Configuration.Type.QUANTITY -> OptionUiState.Quantity(
                                        option
                                    )
                                }
                            }
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

    private fun onOptionSelected(configKey: ConfigurationUiState, optionIndex: Int) =
        withViewModelScope {
            _uiState.update { state ->
                val newConfig = state.configurations.toMutableMap()
                val newOptions = newConfig[configKey]?.toMutableList()
                    ?: throw RuntimeException("Invalid state configuration selected")
                val option = newOptions[optionIndex]
                when (option) {
                    is OptionUiState.MultipleChoice -> {
                        newOptions[optionIndex] = option.copy(isSelected = !option.isSelected)
                    }

                    is OptionUiState.SingleChoice -> {
                        newOptions.replaceAll { (it as OptionUiState.SingleChoice).copy(isSelected = false) }
                        newOptions[optionIndex] = option.copy(isSelected = true)
                    }

                    is OptionUiState.Quantity -> Unit
                }
                newConfig[configKey] = newOptions.toList()
                state.copy(configurations = newConfig)
            }
        }

    private fun onQuantityChanged(configKey: ConfigurationUiState, optionIndex: Int, amount: Int) =
        withViewModelScope {
            _uiState.update { state ->
                val newConfig = state.configurations.toMutableMap()
                val newOptions = newConfig[configKey]?.toMutableList()
                    ?: throw RuntimeException("Invalid state configuration selected")
                val option = newOptions[optionIndex]
                if (option is OptionUiState.Quantity) {
                    newOptions[optionIndex] = option.copy(amount = amount.coerceIn(0, MAX_QUANTITY))
                }
                newConfig[configKey] = newOptions.toList()
                state.copy(configurations = newConfig)
            }
        }

    private fun onNotesChanged(value: String) = withViewModelScope {
        if (value.length > MAX_NOTES_LENGTH) {
            return@withViewModelScope
        }
        _uiState.update { state ->
            state.copy(notes = value)
        }
    }

    companion object {
        const val MAX_QUANTITY = 50
        const val MAX_NOTES_LENGTH = 200
    }
}
