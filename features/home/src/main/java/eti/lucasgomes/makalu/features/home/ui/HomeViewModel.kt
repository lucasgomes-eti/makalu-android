package eti.lucasgomes.makalu.features.home.ui

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.ext.withViewModelScope
import eti.lucasgomes.makalu.features.home.HomeClient
import eti.lucasgomes.makalu.features.home.R
import eti.lucasgomes.makalu.features.home.ui.model.CategoryUiState
import eti.lucasgomes.makalu.features.home.ui.model.HomeAction
import eti.lucasgomes.makalu.features.home.ui.model.HomeUiState
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.network.HttpClientManager.Companion.BASE_URL
import eti.lucasgomes.makalu.shared.network.onError
import eti.lucasgomes.makalu.shared.network.onSuccess
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
    private val navigator: Navigator,
    private val homeClient: HomeClient,
    private val dataStore: DataStore<Settings>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.InitialFetch -> onInitialFetch()
            HomeAction.RefreshStores -> onRefreshStores()
            is HomeAction.StoreClicked -> onStoreClicked(action.id)
            HomeAction.AuthClicked -> onAuthClicked()
            HomeAction.AuthDialogDismissed -> onAuthDialogDismissed()
            is HomeAction.CategoryClicked -> onCategoryClicked(action.index)
            HomeAction.AddressClicked -> onAddressClicked()
        }
    }

    private fun onInitialFetch() {
        startLoading()
        viewModelScope.launch { fetchAddress() }
        viewModelScope.launch { fetchCategories() }
        viewModelScope.launch { fetchStores() }
    }

    private suspend fun fetchAddress() {
        homeClient.getSelfAddress().onSuccess { response ->
            dataStore.updateData { settings ->
                settings.copy(
                    addressId = response.id,
                    addressName = "${response.street}, ${response.number}"
                )
            }
        }
        dataStore.data.collect { settings ->
            val address = settings.addressName
            if (address.isNullOrBlank()) {
                _uiState.update { state ->
                    state.copy(
                        address = UiText.StringResource(R.string.add_a_delivery_address),
                        isAddressLoading = false
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(isAddressLoading = false, address = UiText.PlainText(address))
                }
            }
        }
    }

    private suspend fun fetchCategories() {
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
    }

    private fun startLoading() {
        _uiState.update { state ->
            state.copy(
                isAddressLoading = true,
                isFiltersLoading = true,
                isStoresLoading = true,
                stores = listOf(StoreUiState.Loading)
            )
        }
    }

    private suspend fun fetchStores() {
        homeClient.getStores(_uiState.value.selectedCategoriesIds).onError {
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

    private fun onStoreClicked(id: Long) = withViewModelScope {
        navigator.navigate(Destination.Screen.Store(id))
    }

    private fun onAuthClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isAuthDialogVisible = false) }
        navigator.navigate(Destination.Screen.Login)
    }

    private fun onAuthDialogDismissed() = withViewModelScope {
        _uiState.update { state -> state.copy(isAuthDialogVisible = false) }
    }

    private fun onCategoryClicked(index: Int) = withViewModelScope {
        val list = _uiState.value.categories.toMutableList()
        list[index] = list[index].copy(isSelected = list[index].isSelected.not())
        _uiState.update { state -> state.copy(categories = list) }
        fetchStores()
    }

    private fun onAddressClicked() = withViewModelScope {
        navigator.navigate(Destination.Screen.Address())
    }
}