package eti.lucasgomes.makalu.features.orders.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OrderDetailEntry(innerPadding: PaddingValues, orderId: Long) {

    val viewModel = koinViewModel<OrderDetailViewModel> { parametersOf(orderId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenContainer(innerPadding, hasTopBar = true, hasBottomBar = false) {
        OrderDetailScreen(uiState, viewModel::onAction)
    }
}
