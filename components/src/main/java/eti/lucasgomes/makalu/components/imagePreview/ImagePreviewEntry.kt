package eti.lucasgomes.makalu.components.imagePreview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.components.ScreenContainer
import eti.lucasgomes.makalu.components.imagePreview.ui.ImagePreviewScreen
import eti.lucasgomes.makalu.components.imagePreview.ui.ImagePreviewViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ImagePreviewEntry(innerPadding: PaddingValues) {
    ScreenContainer(innerPadding, hasTopBar = true, hasBottomBar = false) {
        val viewModel = koinViewModel<ImagePreviewViewModel>()
        ImagePreviewScreen(viewModel::onAction)
    }
}