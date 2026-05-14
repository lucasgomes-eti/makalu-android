package eti.lucasgomes.features.cart

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.features.cart.ui.CartScreen
import eti.lucasgomes.features.cart.ui.CartViewModel
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CartEntry(innerPadding: PaddingValues, storeId: Long) {

    val viewModel = koinViewModel<CartViewModel> { parametersOf(storeId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = true) {
        CartScreen(uiState, viewModel::onAction)
    }
}