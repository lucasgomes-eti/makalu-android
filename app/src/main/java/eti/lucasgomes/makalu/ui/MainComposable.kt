package eti.lucasgomes.makalu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import eti.lucasgomes.makalu.BottomNavigationBar
import eti.lucasgomes.makalu.HomeEntry
import eti.lucasgomes.makalu.LocalBottomNavigationBar
import eti.lucasgomes.makalu.LoginEntry
import eti.lucasgomes.makalu.navigation.BottomNavigationItem
import eti.lucasgomes.makalu.navigation.Destination
import eti.lucasgomes.makalu.navigation.NavigationAction
import eti.lucasgomes.makalu.navigation.Navigator
import eti.lucasgomes.makalu.navigation.ObserveAsEvents
import eti.lucasgomes.makalu.navigation.bindNavigationOptions
import eti.lucasgomes.makalu.ui.theme.MakaluTheme
import org.koin.compose.koinInject

@Composable
fun MainComposable() {
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

        val bottomNavigationItems = listOf(
            BottomNavigationItem("Home", Destination.HomeScreen, Icons.Default.Home),
        )

        val bottomNavigationBar = remember {
            BottomNavigationBar(isBottomBarVisible = mutableStateOf(true))
        }

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = bottomNavigationBar.isBottomBarVisible.value,
                    enter = fadeIn() + slideIn { IntOffset(0, it.height) },
                    exit = slideOut { IntOffset(0, it.height) } + fadeOut()
                ) {
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        bottomNavigationItems.forEach { topLevelRoute ->
                            NavigationBarItem(
                                icon = { Icon(topLevelRoute.icon, null) },
                                label = { Text(topLevelRoute.name) },
                                selected = currentDestination?.hierarchy?.any { destination ->
                                    destination.hasRoute(topLevelRoute.route::class)
                                } == true,
                                onClick = {
                                    navController.navigate(topLevelRoute.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // re-selecting the same item
                                        launchSingleTop = true
                                        // Restore state when re-selecting a previously selected item
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            CompositionLocalProvider(LocalBottomNavigationBar provides bottomNavigationBar) {
                NavHost(
                    navController = navController,
                    startDestination = navigator.startDestination
                ) {

                    navigation<Destination.AuthGraph>(startDestination = Destination.LoginScreen) {
                        composable<Destination.LoginScreen> { LoginEntry(innerPadding) }
                    }

                    navigation<Destination.HomeGraph>(startDestination = Destination.HomeScreen) {

                        composable<Destination.HomeScreen> { HomeEntry() }
                    }
                }
            }
        }
    }
}