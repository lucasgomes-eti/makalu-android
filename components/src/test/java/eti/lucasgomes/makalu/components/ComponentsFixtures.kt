package eti.lucasgomes.makalu.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import eti.lucasgomes.makalu.components.dsl.TextFieldErrorsAssignable
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.shared.network.MakaluError

fun testPainter(color: Color = Color.Red): Painter = ColorPainter(color)

fun fieldError(field: String, message: String) = MakaluError.FieldError(field, message)

data class FakeErrorsAssignable(val errors: Map<String, UiText>) : TextFieldErrorsAssignable {
    override fun assignFieldErrors(fieldErrors: List<MakaluError.FieldError>): TextFieldErrorsAssignable = this
}
