package eti.lucasgomes.makalu

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeEntry() {
    val viewModel = koinViewModel<HomeViewModel>()
    HomeScreen { viewModel.goToLogin() }
}