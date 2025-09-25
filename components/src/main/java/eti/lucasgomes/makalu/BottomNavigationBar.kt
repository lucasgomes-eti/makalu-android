package eti.lucasgomes.makalu

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

class BottomNavigationBar(val isBottomBarVisible: MutableState<Boolean>) {

    /** Default state is 'visible'**/
    fun show() {
        isBottomBarVisible.value = true
    }

    /** Default state is 'visible'**/
    fun hide() {
        isBottomBarVisible.value = false
    }
}

val LocalBottomNavigationBar = compositionLocalOf<BottomNavigationBar> {
    error("No BottomNavigationBar provided")
}
