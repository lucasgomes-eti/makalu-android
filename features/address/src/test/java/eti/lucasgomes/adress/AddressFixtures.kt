package eti.lucasgomes.adress

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.shared.model.AddressResponse
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

fun addressResponse(
    id: Long = 7,
    zipCode: String = "01310100",
    street: String = "Av. Paulista",
    number: String? = "1000",
    complement: String? = "Apt 12",
    latitude: Double = -23.56,
    longitude: Double = -46.65,
) = AddressResponse(
    id = id,
    zipCode = zipCode,
    street = street,
    number = number,
    complement = complement,
    longitude = longitude,
    latitude = latitude
)

fun makaluError(httpCode: Int, message: String = "Error", internalCode: String = "MK-1") =
    MakaluError(httpCode = httpCode, message = message, internalCode = internalCode)

fun AddressClient.withGeocodingClient(client: HttpClient): AddressClient = apply {
    AddressClient::class.java.getDeclaredField("geocodingClient").apply {
        isAccessible = true
        set(this@withGeocodingClient, client)
    }
}

class FakeSettingsDataStore(initial: Settings = Settings()) : DataStore<Settings> {
    private val state = MutableStateFlow(initial)

    val current: Settings get() = state.value

    override val data: Flow<Settings> = state

    override suspend fun updateData(transform: suspend (t: Settings) -> Settings): Settings =
        transform(state.value).also { state.value = it }
}
