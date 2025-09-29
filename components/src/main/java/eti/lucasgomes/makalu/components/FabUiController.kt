package eti.lucasgomes.makalu.components

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

class FabUiController {
    val isFabVisible: MutableState<Boolean> = mutableStateOf(false)
    val action: MutableState<FabAction?> = mutableStateOf(null)

    /** Default state is 'hidden'**/
    fun show() {
        isFabVisible.value = true
    }

    /** Default state is 'hidden'**/
    fun hide() {
        isFabVisible.value = false
    }

    fun setAction(action: FabAction) {
        this.action.value = action
    }
}

val LocalFabUiController = compositionLocalOf<FabUiController> {
    error("No Float Action Button provided")
}
