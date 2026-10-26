package eti.lucasgomes.makalu.features.auth

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsDataStore(initial: Settings = Settings()) : DataStore<Settings> {
    private val state = MutableStateFlow(initial)

    val current: Settings get() = state.value

    override val data: Flow<Settings> = state

    override suspend fun updateData(transform: suspend (t: Settings) -> Settings): Settings {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

fun makaluError(
    httpCode: Int = 400,
    message: String = "Bad request",
    internalCode: String = "MK-1",
    fieldErrors: List<MakaluError.FieldError> = emptyList()
) = MakaluError(
    httpCode = httpCode,
    message = message,
    internalCode = internalCode,
    fieldErrors = fieldErrors
)

fun makaluErrorJson(
    httpCode: Int,
    message: String,
    internalCode: String,
    fieldErrors: List<MakaluError.FieldError> = emptyList()
): String {
    val fields = fieldErrors.joinToString(",") {
        """{"field":"${it.field}","message":"${it.message}"}"""
    }
    return """{"http_code":$httpCode,"message":"$message",""" +
        """"internal_code":"$internalCode","field_errors":[$fields]}"""
}

fun tokenPairJson(accessToken: String, refreshToken: String) =
    """{"access_token":"$accessToken","refresh_token":"$refreshToken"}"""

fun registerResponseJson(id: Long, name: String, email: String, phoneNumber: String) =
    """{"id":$id,"name":"$name","email":"$email","phone_number":"$phoneNumber"}"""
