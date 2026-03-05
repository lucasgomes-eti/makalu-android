package eti.lucasgomes.adress

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.adress.ui.AddressScreen
import eti.lucasgomes.adress.ui.AddressViewModel
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddressEntry(innerPadding: PaddingValues) {
    val viewModel = koinViewModel<AddressViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = true) {
        AddressScreen(
            uiState,
            viewModel::onAction
        )
    }
}