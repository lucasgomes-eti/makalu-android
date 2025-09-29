package eti.lucasgomes.makalu.features.home.ui

import androidx.lifecycle.ViewModel
import eti.lucasgomes.makalu.components.ext.withScreenModelScope
import eti.lucasgomes.makalu.features.home.ui.model.CategoryUiState
import eti.lucasgomes.makalu.features.home.ui.model.HomeAction
import eti.lucasgomes.makalu.features.home.ui.model.HomeUiState
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class HomeViewModel(private val navigator: Navigator) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        withScreenModelScope {
            _uiState.update { state ->
                state.copy(
                    address = "4140 Parker Rd. Allentown",
                    categories = listOf(
                        CategoryUiState("All", true),
                        CategoryUiState("Burgers", false),
                        CategoryUiState("Pizza", false),
                        CategoryUiState("Sushi", false),
                        CategoryUiState("Italian", false),
                        CategoryUiState("Chinese", false),
                    ),
                    stores = listOf(
                        StoreUiState(
                            "McDonald's",
                            "Burgers",
                            logoUrl = "https://logodix.com/logo/35948.jpg",
                            coverUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=999&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                        ),
                        StoreUiState(
                            "Domino's",
                            "Pizza",
                            logoUrl = "https://logodix.com/logo/1066761.png",
                            coverUrl = "https://images.unsplash.com/photo-1579751626657-72bc17010498?q=80&w=1169&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                        ),
                    )
                )
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.StoreClicked -> onStoreClicked()
            HomeAction.AuthClicked -> onAuthClicked()
            HomeAction.AuthDialogDismissed -> onAuthDialogDismissed()
        }
    }

    private fun onStoreClicked() = withScreenModelScope {
        _uiState.update { state -> state.copy(isAuthDialogVisible = true) }
    }

    private fun onAuthClicked() = withScreenModelScope {
        _uiState.update { state -> state.copy(isAuthDialogVisible = false) }
        navigator.navigate(Destination.Screen.Login)
    }

    private fun onAuthDialogDismissed() = withScreenModelScope {
        _uiState.update { state -> state.copy(isAuthDialogVisible = false) }
    }
}