package eti.lucasgomes.makalu.ui.appScaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import eti.lucasgomes.makalu.components.imageCropper.ImageCropperEntry
import eti.lucasgomes.makalu.features.auth.login.LoginEntry
import eti.lucasgomes.makalu.features.auth.registration.RegistrationEntry
import eti.lucasgomes.makalu.features.home.HomeEntry
import eti.lucasgomes.makalu.features.orders.OrdersEntry
import eti.lucasgomes.makalu.features.profile.ProfileEntry
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.Navigator

@Composable
fun NavigationGraph(
    concreteNavigator: NavHostController,
    abstractNavigator: Navigator,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = concreteNavigator,
        startDestination = abstractNavigator.startDestination
    ) {
        navigation<Destination.Graph.Auth>(startDestination = Destination.Screen.Login) {
            composable<Destination.Screen.Login> { LoginEntry(innerPadding) }
            composable<Destination.Screen.Registration> { RegistrationEntry(innerPadding) }
            composable<Destination.Screen.ImagePreview> {
                ImageCropperEntry(innerPadding, it.toRoute<Destination.Screen.ImagePreview>().uri)
            }
        }

        navigation<Destination.Graph.Home>(startDestination = Destination.Screen.Home) {
            composable<Destination.Screen.Home> { HomeEntry(innerPadding) }
            composable<Destination.Screen.Orders> { OrdersEntry(innerPadding) }
            composable<Destination.Screen.Profile> { ProfileEntry(innerPadding) }
        }
    }
}