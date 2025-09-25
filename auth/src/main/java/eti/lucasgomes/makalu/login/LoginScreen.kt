package eti.lucasgomes.makalu.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun LoginScreen(onHome: () -> Unit, onRegistration: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login")
        Button(onClick = onHome) { Text("Go to home") }
        Button(onClick = onRegistration) { Text("Go to registration") }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    LoginScreen({ }, {})
}