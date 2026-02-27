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
        delay(1_000L)
        _uiState.update { state ->
            state.copy(
                stores = listOf(StoreUiState.NoContent),
                isStoresLoading = false,
            )
        }
    }

    private fun onRefreshStores() = withViewModelScope {
        _uiState.update { state -> state.copy(isStoresLoading = true) }
        delay(2_000L)
        _uiState.update { state ->
            state.copy(
                stores = listOf(
                    StoreUiState.Data(
                        "McDonald's",
                        "Burgers",
                        logoUrl = "https://logodix.com/logo/35948.jpg",
                        coverUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=999&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                    ),
                    StoreUiState.Data(
                        "Domino's",
                        "Pizza",
                        logoUrl = "https://logodix.com/logo/1066761.png",
                        coverUrl = "https://images.unsplash.com/photo-1579751626657-72bc17010498?q=80&w=1169&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                    ),
                ),
                isStoresLoading = false
            )
        }
    }

    private fun onStoreClicked() = withViewModelScope {
        _uiState.update { state -> state.copy(isAuthDialogVisible = true) }
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
}