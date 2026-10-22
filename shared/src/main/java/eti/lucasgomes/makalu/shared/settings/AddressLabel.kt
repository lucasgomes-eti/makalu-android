package eti.lucasgomes.makalu.shared.settings

fun addressLabel(street: String, number: String?): String =
    if (number.isNullOrBlank()) street else "$street, $number"
