package eti.lucasgomes.makalu.features.home.ui

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.shared.model.AddressResponse
import eti.lucasgomes.makalu.shared.model.CategoryResponse
import eti.lucasgomes.makalu.shared.model.StoreResponse
import eti.lucasgomes.makalu.shared.network.MakaluError
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal

internal class FakeSettingsDataStore(initial: Settings = Settings()) : DataStore<Settings> {

    private val state = MutableStateFlow(initial)
    private val mutex = Mutex()

    val current: Settings
        get() = state.value

    override val data: Flow<Settings> = state

    override suspend fun updateData(transform: suspend (t: Settings) -> Settings): Settings =
        mutex.withLock {
            transform(state.value).also { state.value = it }
        }
}

internal fun categoryResponse(id: Long = 1, description: String = "Pizza") =
    CategoryResponse(id, description)

internal fun storeResponse(
    id: Long = 1,
    name: String = "Scary's",
    categories: List<CategoryResponse> = listOf(categoryResponse()),
    logoImageId: Long? = 3,
    coverImageId: Long? = null,
    deliveryFee: BigDecimal = BigDecimal("4.99"),
) = StoreResponse(id, name, categories, logoImageId, coverImageId, deliveryFee)

internal fun addressResponse(
    id: Long = 5,
    street: String = "Main St",
    number: String? = "12",
) = AddressResponse(
    id = id,
    zipCode = "12345-000",
    street = street,
    number = number,
    complement = null,
    longitude = -46.6,
    latitude = -23.5,
)

internal fun makaluError(httpCode: Int = 500) = MakaluError(
    httpCode = httpCode,
    message = "Something went wrong",
    internalCode = "MK-1",
)
