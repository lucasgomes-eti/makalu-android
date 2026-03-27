package eti.lucasgomes.makalu.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import eti.lucasgomes.makalu.components.appBars.LocalBottomBarUiController
import eti.lucasgomes.makalu.components.appBars.LocalTopBarUiController
import eti.lucasgomes.makalu.components.buttons.LocalFabUiController

@Composable
fun ScreenContainer(
    innerPadding: PaddingValues,
    hasBottomBar: Boolean = true,
    hasTopBar: Boolean = false,
    hasFab: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val bottomBar = LocalBottomBarUiController.current
    val topBar = LocalTopBarUiController.current
    val fab = LocalFabUiController.current
    LaunchedEffect(Unit) {
        if (hasBottomBar) bottomBar.show() else bottomBar.hide()
        if (hasTopBar) topBar.show() else topBar.hide()
        if (hasFab) fab.show() else fab.hide()
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) { content() }
}