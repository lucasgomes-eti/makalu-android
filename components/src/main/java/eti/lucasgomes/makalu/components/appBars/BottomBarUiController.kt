package eti.lucasgomes.makalu.components.appBars

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

class BottomBarUiController {
    val isBottomBarVisible: MutableState<Boolean> = mutableStateOf(true)

    /** Default state is 'visible'**/
    fun show() {
        isBottomBarVisible.value = true
    }

    /** Default state is 'visible'**/
    fun hide() {
        isBottomBarVisible.value = false
    }
}

val LocalBottomBarUiController = compositionLocalOf<BottomBarUiController> {
    error("No BottomBar provided")
}
