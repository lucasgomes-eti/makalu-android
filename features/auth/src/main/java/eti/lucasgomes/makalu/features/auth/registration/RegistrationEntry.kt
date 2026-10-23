package eti.lucasgomes.makalu.features.auth.registration

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationAction
import eti.lucasgomes.makalu.features.auth.registration.ui.RegistrationScreen
import eti.lucasgomes.makalu.features.auth.registration.ui.RegistrationViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegistrationEntry(innerPadding: PaddingValues, croppedImageUris: Flow<String>) {
    ScreenContainer(innerPadding, hasBottomBar = false, hasTopBar = true) {
        val viewModel = koinViewModel<RegistrationViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(croppedImageUris) {
            croppedImageUris.collect {
                viewModel.onAction(RegistrationAction.CroppedImageReceived(it.toUri()))
            }
        }
        RegistrationScreen(uiState, viewModel::onAction)
    }
}