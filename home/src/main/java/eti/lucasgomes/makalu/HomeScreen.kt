package eti.lucasgomes.makalu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeScreen(onAction: () -> Unit) {
    Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(Greeting.NAME)
            Text("Home")
            Button(onClick = onAction) { Text("Go to login") }
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    HomeScreen() {}
}