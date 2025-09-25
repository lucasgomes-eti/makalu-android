package eti.lucasgomes.makalu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginEntry(innerPaddingValues: PaddingValues) {
    val viewModel = koinViewModel<LoginViewModel>()
    LoginScreen { viewModel.goToHome() }
}