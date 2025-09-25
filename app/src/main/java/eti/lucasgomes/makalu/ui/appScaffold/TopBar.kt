package eti.lucasgomes.makalu.ui.appScaffold

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.IntOffset
import eti.lucasgomes.makalu.components.TopBarUiController
import eti.lucasgomes.makalu.shared.navigation.Navigator
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBar(topBarUiController: TopBarUiController) {
    val navigator = koinInject<Navigator>()
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = topBarUiController.isTopBarVisible.value,
        enter = fadeIn() + slideIn { IntOffset(0, -it.height) },
        exit = slideOut { IntOffset(0, -it.height) } + fadeOut()
    ) {
        TopAppBar(
            title = {
                AnimatedContent(
                    targetState = topBarUiController.title.value
                ) { value ->
                    Text(value)
                }
            },
            actions = {
                topBarUiController.actions.forEach { action ->
                    with(action) {
                        IconButton(onClick = onClick) {
                            Icon(icon, contentDescription)
                        }
                    }

                }
            },
            navigationIcon = {
                IconButton(onClick = { scope.launch { navigator.navigateUp() } }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                titleContentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
                actionIconContentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
                navigationIconContentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
            )
        )
    }
}