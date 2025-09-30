package eti.lucasgomes.makalu.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun OnFirstComposition(execute: () -> Unit) {
    var isFirstComposition by rememberSaveable { mutableStateOf(true) }

    if (isFirstComposition) {
        isFirstComposition = false
        execute()
    }
}