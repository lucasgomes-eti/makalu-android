package eti.lucasgomes.makalu.features.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.features.profile.model.ProfileAction
import eti.lucasgomes.makalu.features.profile.ui.ProfileScreen
import eti.lucasgomes.makalu.features.profile.ui.ProfileViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileEntry(innerPadding: PaddingValues, croppedImageUris: Flow<String>) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(croppedImageUris) {
        croppedImageUris.collect {
            viewModel.onAction(ProfileAction.CroppedImageReceived(it.toUri()))
        }
    }
    ScreenContainer(innerPadding) { ProfileScreen(uiState, viewModel::onAction) }
}
