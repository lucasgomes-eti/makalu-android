package eti.lucasgomes.makalu.features.home.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.home.HomeClient
import eti.lucasgomes.makalu.features.home.ui.model.CategoryUiState
import eti.lucasgomes.makalu.features.home.ui.model.HomeAction
import eti.lucasgomes.makalu.features.home.ui.model.HomeUiState
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.HttpClientManager.Companion.BASE_URL
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class HomeViewModel(
    private val navigator: Navigator,
    private val homeClient: HomeClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.InitialFetch -> onInitialFetch()
            HomeAction.RefreshStores -> onRefreshStores()
            HomeAction.StoreClicked -> onStoreClicked()
            HomeAction.AuthClicked -> onAuthClicked()
            HomeAction.AuthDialogDismissed -> onAuthDialogDismissed()
            is HomeAction.CategoryClicked -> onCategoryClicked(action.index)
            HomeAction.AddressClicked -> onAddressClicked()
        }
    }

    private fun onInitialFetch() = withViewModelScope {
        _uiState.update { state ->
            state.copy(
                isAddressLoading = true,
                isFiltersLoading = true,
                isStoresLoading = true,
                stores = listOf(StoreUiState.Loading)
            )
        }

        homeClient.getCategories().onSuccess {
            _uiState.update { state ->
                state.copy(
                    isFiltersLoading = false,
                    categories = it.map { category ->
                        CategoryUiState(
                            category.id,
                            UiText.PlainText(category.description),
                            false
                        )
                    },
                )
            }
        }

        delay(1_000L)
        _uiState.update { state ->
            state.copy(address = "4140 Parker Rd. Allentown", isAddressLoading = false)
        }

        fetchStores()
    }

    private fun fetchStores() = withViewModelScope {
        homeClient.getStores().onError {
            _uiState.update { state ->
                state.copy(
                    stores = listOf(StoreUiState.NoContent),
                    isStoresLoading = false,
                )
            }
        }.onSuccess {
            if (it.isEmpty()) {
                _uiState.update { state ->
                    state.copy(
                        stores = listOf(StoreUiState.NoContent),
                        isStoresLoading = false,
                    )
                }
                return@onSuccess
            }
            _uiState.update { state ->
                state.copy(
                    stores = it.map { store ->
                        StoreUiState.Data(
                            store.id,
                            store.name,
                            store.categories.joinToString(", ") { category -> category.description },
                            logoUrl = mapLogoImageIdToUrl(store.logoImageId),
                            coverUrl = mapCoverImageIdToUrl(store.coverImageId)
                        )
                    },
                    isStoresLoading = false,
                )
            }
        }
    }

    private fun mapLogoImageIdToUrl(imageId: Long?): String? = imageId?.let { id ->
        "${BASE_URL}stores/logo-image/$id"
    }

    private fun mapCoverImageIdToUrl(imageId: Long?): String? = imageId?.let { id ->
        "${BASE_URL}stores/cover-image/$id"
    }

    private fun onRefreshStores() = withViewModelScope {
        _uiState.update { state -> state.copy(isStoresLoading = true) }
        fetchStores()
    }

    private fun onStoreClicked() = withViewModelScope {
        // TODO: Open store menu
    }

    private fun onAuthClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isAuthDialogVisible = false) }
        navigator.navigate(Destination.Screen.Login)
    }

    private fun onAuthDialogDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isAuthDialogVisible = false) }
    }

    private fun onCategoryClicked(index: Int) = withViewModelScope {
        _uiState.update { state ->
            val list = state.categories.toMutableList()
            list[index] = list[index].copy(isSelected = list[index].isSelected.not())
            state.copy(categories = list)
        }
    }

    private fun onAddressClicked() = withViewModelScope {
        navigator.navigate(Destination.Screen.Address())
    }
}