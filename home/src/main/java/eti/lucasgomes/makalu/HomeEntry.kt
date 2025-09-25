package eti.lucasgomes.makalu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeEntry(innerPadding: PaddingValues) {
    val viewModel = koinViewModel<HomeViewModel>()
    ScreenContainer(innerPadding) { HomeScreen { viewModel.goToLogin() } }
}