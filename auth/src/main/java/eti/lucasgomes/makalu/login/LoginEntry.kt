package eti.lucasgomes.makalu.login

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.ScreenContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginEntry(innerPadding: PaddingValues) {
    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = false) {
        val viewModel = koinViewModel<LoginViewModel>()
        LoginScreen(
            onHome = { viewModel.goToHome() },
            onRegistration = { viewModel.goToRegistration() }
        )
    }
}