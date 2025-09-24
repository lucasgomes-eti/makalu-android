package eti.lucasgomes.makalu

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import eti.lucasgomes.makalu.navigation.Destination
import eti.lucasgomes.makalu.navigation.NavigationAction
import eti.lucasgomes.makalu.navigation.Navigator
import eti.lucasgomes.makalu.ui.theme.MakaluTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    MakaluTheme {
        val navController = rememberNavController()
        val navigator = koinInject<Navigator>()

        ObserveAsEvents(navigator.navigationActions) { action ->
            when (action) {
                is NavigationAction.Navigate -> navController.navigate(
                    action.destination,
                    bindNavigationOptions(action.navOptions)
                )

                NavigationAction.NavigateUp -> navController.navigateUp()
            }
        }

        NavHost(navController = navController, startDestination = navigator.startDestination) {

            navigation<Destination.AuthGraph>(startDestination = Destination.LoginScreen) {
                composable<Destination.LoginScreen> { LoginEntry() }
            }

            navigation<Destination.HomeGraph>(startDestination = Destination.HomeScreen) {
                composable<Destination.HomeScreen> { HomeEntry() }
            }
        }
    }
}