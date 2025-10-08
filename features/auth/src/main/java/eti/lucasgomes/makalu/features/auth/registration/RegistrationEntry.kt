package eti.lucasgomes.makalu.features.auth.registration

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.features.auth.registration.ui.RegistrationScreen
import eti.lucasgomes.makalu.features.auth.registration.ui.RegistrationViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegistrationEntry(innerPadding: PaddingValues) {
    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = true) {
        val viewModel = koinViewModel<RegistrationViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        RegistrationScreen(uiState, viewModel::onAction)
    }
}