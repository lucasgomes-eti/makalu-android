package eti.lucasgomes.makalu.features.auth.login

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.features.auth.login.ui.LoginScreen
import eti.lucasgomes.makalu.features.auth.login.ui.LoginViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginEntry(innerPadding: PaddingValues) {
    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = false) {
        val viewModel = koinViewModel<LoginViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        LoginScreen(uiState = uiState, onAction = viewModel::onAction)
    }
}