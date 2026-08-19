package eti.lucasgomes.makalu.features.orders.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun OrdersEntry(innerPadding: PaddingValues) {
    val viewModel = koinViewModel<OrdersViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContainer(innerPadding) { OrdersScreen(uiState, viewModel::onAction) }
}
