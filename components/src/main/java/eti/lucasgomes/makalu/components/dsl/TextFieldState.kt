package eti.lucasgomes.makalu.components.dsl

data class TextFieldState(
    val text: String = "",
    val error: UiText = UiText.Empty,
    val isEnabled: Boolean = true
) {
    val hasError get() = error != UiText.Empty
}