package eti.lucasgomes.makalu.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import eti.lucasgomes.adress.AddressEntry
import eti.lucasgomes.features.checkout.CheckoutEntry
import eti.lucasgomes.features.menuItem.MenuItemEntry
import eti.lucasgomes.features.store.StoreEntry
import eti.lucasgomes.makalu.components.imageCropper.ImageCropperEntry
import eti.lucasgomes.makalu.features.auth.login.LoginEntry
import eti.lucasgomes.makalu.features.auth.registration.RegistrationEntry
import eti.lucasgomes.makalu.features.home.HomeEntry
import eti.lucasgomes.makalu.features.orders.OrdersEntry
import eti.lucasgomes.makalu.features.profile.ProfileEntry
import eti.lucasgomes.makalu.shared.navigation.Destination

@Composable
fun NavigationGraph(
    navHostController: NavHostController,
    innerPadding: PaddingValues,
    hasAccessToken: Boolean
) {
    NavHost(
        navController = navHostController,
        startDestination = if (hasAccessToken) Destination.Graph.Home else Destination.Graph.Auth
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
            composable<Destination.Screen.Address> { AddressEntry(innerPadding) }
            composable<Destination.Screen.Store> {
                StoreEntry(
                    innerPadding,
                    it.toRoute<Destination.Screen.Store>().id
                )
            }
            composable<Destination.Screen.MenuItem> {
                MenuItemEntry(
                    innerPadding,
                    it.toRoute<Destination.Screen.MenuItem>().id
                )
            }
            composable<Destination.Screen.Checkout> {
                CheckoutEntry(innerPadding)
            }
        }
    }
}