package eti.lucasgomes.makalu.features.auth.registration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eti.lucasgomes.makalu.components.ConfigureTopBar
import eti.lucasgomes.makalu.components.TopBarAction

@Composable
fun RegistrationScreen(onHome: () -> Unit) {
    ConfigureTopBar(
        title = "Registration",
        navigationActions = listOf(
            TopBarAction(Icons.Default.Home, "Home") { onHome() }
        ),
    )
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Registration Screen")
    }
}