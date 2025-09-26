package eti.lucasgomes.makalu.features.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.components.ScreenContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeEntry(innerPadding: PaddingValues) {
    val viewModel = koinViewModel<HomeViewModel>()
    ScreenContainer(innerPadding) { HomeScreen { viewModel.goToLogin() } }
}