package eti.lucasgomes.makalu.components

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class TopBarUiController {
    val isTopBarVisible: MutableState<Boolean> = mutableStateOf(false)
    val title: MutableState<String> = mutableStateOf("")

    val actions: SnapshotStateList<TopBarAction> = mutableStateListOf()

    /** Default state is 'hidden'**/
    fun show() {
        isTopBarVisible.value = true
    }

    /** Default state is 'hidden'**/
    fun hide() {
        isTopBarVisible.value = false
    }

    fun setTitle(title: String) {
        this.title.value = title
    }

    fun setNavigationActions(actions: List<TopBarAction>) {
        this.actions.clear()
        this.actions.addAll(actions)
    }
}

val LocalTopBarUiController = compositionLocalOf<TopBarUiController> { error("No TopBar provided") }