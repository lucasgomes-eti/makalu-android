package eti.lucasgomes.makalu.features.orders

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.components.ScreenContainer

@Composable
fun OrdersEntry(innerPadding: PaddingValues) {
    ScreenContainer(innerPadding) { OrdersScreen() }
}