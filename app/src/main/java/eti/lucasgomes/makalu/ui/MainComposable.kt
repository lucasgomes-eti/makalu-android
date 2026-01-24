package eti.lucasgomes.makalu.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import eti.lucasgomes.makalu.components.appBars.BottomBarUiController
import eti.lucasgomes.makalu.components.appBars.LocalBottomBarUiController
import eti.lucasgomes.makalu.components.appBars.LocalTopBarUiController
import eti.lucasgomes.makalu.components.appBars.TopBarUiController
import eti.lucasgomes.makalu.components.buttons.FabUiController
import eti.lucasgomes.makalu.components.buttons.LocalFabUiController
import eti.lucasgomes.makalu.navigation.ObserveAsEvents
import eti.lucasgomes.makalu.navigation.bindNavigationEvents
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.ui.appScaffold.BottomBar
import eti.lucasgomes.makalu.ui.appScaffold.FloatingActionButton
import eti.lucasgomes.makalu.ui.appScaffold.NavigationGraph
import eti.lucasgomes.makalu.ui.appScaffold.TopBar
import eti.lucasgomes.makalu.ui.theme.MakaluTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposable(hasAccessToken: Boolean) {
    MakaluTheme {
        val abstractNavigator = koinInject<Navigator>()
        val concreteNavigator = rememberNavController()

        ObserveAsEvents(
            abstractNavigator.navigationActions,
            onEvent = bindNavigationEvents(concreteNavigator)
        )

        val bottomBarUiController = remember { BottomBarUiController() }
        val topBarUiController = remember { TopBarUiController() }
        val fabUiController = remember { FabUiController() }

        Scaffold(
            topBar = { TopBar(topBarUiController) },
            bottomBar = { BottomBar(bottomBarUiController, concreteNavigator) },
            floatingActionButton = { FloatingActionButton(fabUiController) }
        ) { innerPadding ->
            CompositionLocalProvider(
                LocalBottomBarUiController provides bottomBarUiController,
                LocalTopBarUiController provides topBarUiController,
                LocalFabUiController provides fabUiController
            ) {
                NavigationGraph(
                    navHostController = concreteNavigator,
                    innerPadding = innerPadding,
                    hasAccessToken = hasAccessToken
                )
            }
        }
    }
}