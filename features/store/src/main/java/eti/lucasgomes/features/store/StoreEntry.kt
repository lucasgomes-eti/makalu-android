package eti.lucasgomes.features.store

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.features.store.ui.StoreScreen
import eti.lucasgomes.features.store.ui.StoreViewModel
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StoreEntry(innerPadding: PaddingValues, id: Long) {
    val viewModel = koinViewModel<StoreViewModel> { parametersOf(id) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = false, hasFab = true) {
        StoreScreen(
            uiState,
            viewModel::onAction
        )
    }
}