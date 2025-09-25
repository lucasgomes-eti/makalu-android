package eti.lucasgomes.makalu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ConfigureTopBar(
    title: String,
    navigationActions: List<TopBarAction>,
) {
    val topBar = LocalTopBarUiController.current
    LaunchedEffect(Unit) {
        topBar.setTitle(title)
        topBar.setNavigationActions(navigationActions)
    }
}