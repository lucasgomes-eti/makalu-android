package eti.lucasgomes.makalu.components.dsl

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class StringResource(
        @param:StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    data object Empty : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is StringResource -> stringResource(id, *args.toTypedArray())
            Empty -> ""
        }
    }
}

