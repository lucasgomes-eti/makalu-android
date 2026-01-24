package eti.lucasgomes.makalu.features.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.features.profile.ui.ProfileScreen
import eti.lucasgomes.makalu.features.profile.ui.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileEntry(innerPadding: PaddingValues) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContainer(innerPadding) { ProfileScreen(uiState, viewModel::onAction) }
}