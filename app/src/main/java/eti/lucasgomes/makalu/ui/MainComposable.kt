package eti.lucasgomes.makalu.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import eti.lucasgomes.makalu.components.BottomBarUiController
import eti.lucasgomes.makalu.components.LocalBottomBarUiController
import eti.lucasgomes.makalu.components.LocalTopBarUiController
import eti.lucasgomes.makalu.components.TopBarUiController
import eti.lucasgomes.makalu.features.auth.login.LoginEntry
import eti.lucasgomes.makalu.features.auth.registration.RegistrationEntry
import eti.lucasgomes.makalu.features.home.HomeEntry
import eti.lucasgomes.makalu.navigation.BottomNavigationItem
import eti.lucasgomes.makalu.navigation.ObserveAsEvents
import eti.lucasgomes.makalu.navigation.bindNavigationOptions
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavigationAction
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.ui.theme.MakaluTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
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
            BottomNavigationItem("Home", Destination.Screen.Home, Icons.Default.Home),
        )

        val bottomBarUiController = remember { BottomBarUiController() }
        val topBarUiController = remember { TopBarUiController() }

        Scaffold(
            topBar = {
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
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colorScheme.surfaceContainer,
                            titleContentColor = colorScheme.contentColorFor(colorScheme.surfaceContainer),
                            actionIconContentColor = colorScheme.contentColorFor(colorScheme.surfaceContainer),
                            navigationIconContentColor = colorScheme.contentColorFor(colorScheme.surfaceContainer),
                        )
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = bottomBarUiController.isBottomBarVisible.value,
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
            CompositionLocalProvider(
                LocalBottomBarUiController provides bottomBarUiController,
                LocalTopBarUiController provides topBarUiController
            ) {
                NavHost(
                    navController = navController,
                    startDestination = navigator.startDestination
                ) {
                    navigation<Destination.Graph.Auth>(startDestination = Destination.Screen.Login) {
                        composable<Destination.Screen.Login> { LoginEntry(innerPadding) }
                        composable<Destination.Screen.Registration> { RegistrationEntry(innerPadding) }
                    }

                    navigation<Destination.Graph.Home>(startDestination = Destination.Screen.Home) {
                        composable<Destination.Screen.Home> { HomeEntry(innerPadding) }
                    }
                }
            }
        }
    }
}