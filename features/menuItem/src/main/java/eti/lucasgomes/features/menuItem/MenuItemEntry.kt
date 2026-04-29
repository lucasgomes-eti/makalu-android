package eti.lucasgomes.features.menuItem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.features.menuItem.ui.MenuItemScreen
import eti.lucasgomes.features.menuItem.ui.MenuItemViewModel
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MenuItemEntry(innerPadding: PaddingValues, id: Long) {
    val viewModel = koinViewModel<MenuItemViewModel> { parametersOf(id) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = false, hasFab = true) {
        MenuItemScreen(uiState, viewModel::onAction)
    }
}