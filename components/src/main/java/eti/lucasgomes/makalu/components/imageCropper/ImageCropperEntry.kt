package eti.lucasgomes.makalu.components.imageCropper

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.components.imageCropper.ui.ImageCropperScreen
import eti.lucasgomes.makalu.components.imageCropper.ui.ImageCropperViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ImageCropperEntry(innerPadding: PaddingValues, uri: String) {
    ScreenContainer(innerPadding, hasTopBar = true, hasBottomBar = false) {
        val viewModel = koinViewModel<ImageCropperViewModel>(parameters = { parametersOf(uri) })
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ImageCropperScreen(uiState, viewModel::onAction)
    }
}