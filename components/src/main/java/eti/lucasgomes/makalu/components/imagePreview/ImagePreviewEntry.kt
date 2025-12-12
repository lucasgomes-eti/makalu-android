package eti.lucasgomes.makalu.components.imagePreview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.components.imagePreview.ui.ImagePreviewScreen
import eti.lucasgomes.makalu.components.imagePreview.ui.ImagePreviewViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ImagePreviewEntry(innerPadding: PaddingValues, uri: String) {
    ScreenContainer(innerPadding, hasTopBar = true, hasBottomBar = false) {
        val viewModel = koinViewModel<ImagePreviewViewModel>(parameters = { parametersOf(uri) })
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ImagePreviewScreen(uiState, viewModel::onAction)
    }
}