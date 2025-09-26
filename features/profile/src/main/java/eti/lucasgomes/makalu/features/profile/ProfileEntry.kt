package eti.lucasgomes.makalu.features.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import eti.lucasgomes.makalu.components.ScreenContainer

@Composable
fun ProfileEntry(innerPadding: PaddingValues) {
    ScreenContainer(innerPadding) { ProfileScreen() }
}