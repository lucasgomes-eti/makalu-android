package eti.lucasgomes.makalu.components.ext

import eti.lucasgomes.makalu.components.dsl.TextFieldErrorsAssignable
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.network.MakaluError

fun <T : TextFieldErrorsAssignable> List<MakaluError.FieldError>.withFieldErrorsAsMap(block: Map<String, UiText>.() -> T): T {
    return associate { it.field to UiText.PlainText(it.message) }.run { block() }
}