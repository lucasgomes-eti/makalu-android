package eti.lucasgomes.makalu.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import eti.lucasgomes.makalu.components.BottomBarUiController
import eti.lucasgomes.makalu.components.LocalBottomBarUiController
import eti.lucasgomes.makalu.components.LocalTopBarUiController
import eti.lucasgomes.makalu.components.TopBarUiController
import eti.lucasgomes.makalu.navigation.ObserveAsEvents
import eti.lucasgomes.makalu.navigation.bindNavigationEvents
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.ui.appScaffold.BottomBar
import eti.lucasgomes.makalu.ui.appScaffold.NavigationGraph
import eti.lucasgomes.makalu.ui.appScaffold.TopBar
import eti.lucasgomes.makalu.ui.theme.MakaluTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposable() {
    MakaluTheme {
        val abstractNavigator = koinInject<Navigator>()
        val concreteNavigator = rememberNavController()

        ObserveAsEvents(
            abstractNavigator.navigationActions,
            onEvent = bindNavigationEvents(concreteNavigator)
        )

        val bottomBarUiController = remember { BottomBarUiController() }
        val topBarUiController = remember { TopBarUiController() }

        Scaffold(
            topBar = { TopBar(topBarUiController) },
            bottomBar = { BottomBar(bottomBarUiController, concreteNavigator) }
        ) { innerPadding ->
            CompositionLocalProvider(
                LocalBottomBarUiController provides bottomBarUiController,
                LocalTopBarUiController provides topBarUiController
            ) {
                NavigationGraph(concreteNavigator, abstractNavigator, innerPadding)
            }
        }
    }
}