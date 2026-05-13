package eti.lucasgomes.features.checkout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.features.checkout.ui.CheckoutScreen
import eti.lucasgomes.features.checkout.ui.CheckoutViewModel
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckoutEntry(innerPadding: PaddingValues) {

    val viewModel = koinViewModel<CheckoutViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = true) {
        CheckoutScreen(uiState, viewModel::onAction)
    }
}