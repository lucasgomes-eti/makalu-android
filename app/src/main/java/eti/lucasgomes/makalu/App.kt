package eti.lucasgomes.makalu

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eti.lucasgomes.makalu.ui.theme.MakaluTheme

@Composable
fun App() {
    MakaluTheme {
        Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
            Home(innerPadding)
        }
    }
}