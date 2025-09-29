package eti.lucasgomes.makalu.features.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.features.home.ui.HomeScreen
import eti.lucasgomes.makalu.features.home.ui.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeEntry(innerPadding: PaddingValues) {
    val viewModel = koinViewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContainer(innerPadding) { HomeScreen(uiState) }
}