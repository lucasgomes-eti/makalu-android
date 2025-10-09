package eti.lucasgomes.makalu.components.imagePreview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.components.ScreenContainer

@Composable
fun ImagePreviewEntry(innerPadding: PaddingValues) {
    ScreenContainer(innerPadding, hasTopBar = true, hasBottomBar = false) {
        ImagePreviewScreen()
    }
}