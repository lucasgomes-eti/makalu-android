package eti.lucasgomes.makalu.features.auth.registration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar

@Composable
fun RegistrationScreen(onHome: () -> Unit) {
    ConfigureTopBar(
        title = "Registration",
        navigationActions = listOf(),
    )
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Registration Screen")
    }
}