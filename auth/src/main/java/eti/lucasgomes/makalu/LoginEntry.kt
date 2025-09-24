package eti.lucasgomes.makalu

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginEntry() {
    val viewModel = koinViewModel<LoginViewModel>()
    LoginScreen { viewModel.goToHome() }
}