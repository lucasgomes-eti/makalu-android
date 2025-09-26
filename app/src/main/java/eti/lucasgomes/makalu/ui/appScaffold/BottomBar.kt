package eti.lucasgomes.makalu.ui.appScaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import eti.lucasgomes.makalu.R
import eti.lucasgomes.makalu.components.BottomBarUiController
import eti.lucasgomes.makalu.navigation.BottomNavigationItem
import eti.lucasgomes.makalu.shared.navigation.Destination

@Composable
fun BottomBar(
    bottomBarUiController: BottomBarUiController,
    navHostController: NavHostController,
) {
    val bottomNavigationItems = listOf(
        BottomNavigationItem(
            name = "Home",
            route = Destination.Screen.Home,
            defaultIcon = painterResource(R.drawable.outline_home),
            selectedIcon = painterResource(R.drawable.filled_home),
            contentDescription = "Home"
        ),
        BottomNavigationItem(
            name = "Orders",
            route = Destination.Screen.Orders,
            defaultIcon = painterResource(R.drawable.outline_receipt),
            selectedIcon = painterResource(R.drawable.filled_receipt),
            contentDescription = "Orders"
        ),
        BottomNavigationItem(
            name = "Profile",
            route = Destination.Screen.Profile,
            defaultIcon = painterResource(R.drawable.outline_person),
            selectedIcon = painterResource(R.drawable.filled_person),
            contentDescription = "Profile"
        ),
    )

    AnimatedVisibility(
        visible = bottomBarUiController.isBottomBarVisible.value,
        enter = fadeIn() + slideIn { IntOffset(0, it.height) },
        exit = slideOut { IntOffset(0, it.height) } + fadeOut()
    ) {
        NavigationBar {
            val navBackStackEntry by navHostController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            bottomNavigationItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { destination ->
                    destination.hasRoute(item.route::class)
                } == true
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selected) item.selectedIcon else item.defaultIcon,
                            item.contentDescription
                        )
                    },
                    label = { Text(item.name) },
                    selected = selected,
                    onClick = {
                        navHostController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navHostController.graph.findStartDestination().id) {
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