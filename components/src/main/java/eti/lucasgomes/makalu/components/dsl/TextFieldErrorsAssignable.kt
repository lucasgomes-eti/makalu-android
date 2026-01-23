package eti.lucasgomes.makalu.components.dsl

import eti.lucasgomes.makalu.shared.network.MakaluError

interface TextFieldErrorsAssignable {
    fun assignFieldErrors(fieldErrors: List<MakaluError.FieldError>): TextFieldErrorsAssignable
}