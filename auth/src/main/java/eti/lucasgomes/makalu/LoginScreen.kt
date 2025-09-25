package eti.lucasgomes.makalu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun LoginScreen(onAction: () -> Unit) {

    val bottomNavigationBar = LocalBottomNavigationBar.current

    DisposableEffect(Unit) {
        bottomNavigationBar.hide()
        onDispose { bottomNavigationBar.show() }
    }

    Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login")
            Button(onClick = onAction) { Text("Go to home") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    LoginScreen() {}
}