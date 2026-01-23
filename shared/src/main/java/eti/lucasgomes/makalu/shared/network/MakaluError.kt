package eti.lucasgomes.makalu.shared.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MakaluError(
    @SerialName("http_code")
    val httpCode: Int,
    val message: String,
    @SerialName("internal_code")
    val internalCode: String,
    @SerialName("field_errors")
    val fieldErrors: List<FieldError> = emptyList()
) {
    val formatedMessage get() = "$internalCode - $message"

    @Serializable
    data class FieldError(val field: String, val message: String)
}
