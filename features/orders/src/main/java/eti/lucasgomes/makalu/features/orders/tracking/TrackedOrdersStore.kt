package eti.lucasgomes.makalu.features.orders.tracking

import androidx.datastore.core.DataStore
import eti.lucasgomes.makalu.shared.settings.Settings
import kotlinx.coroutines.flow.first

internal class TrackedOrdersStore(private val dataStore: DataStore<Settings>) {
    suspend fun read(): Set<Long> = dataStore.data.first().trackedOrderIds.toSet()

    suspend fun write(ids: Set<Long>) {
        dataStore.updateData { settings ->
            val sorted = ids.sorted()
            if (settings.trackedOrderIds ==
                sorted
            ) {
                settings
            } else {
                settings.copy(trackedOrderIds = sorted)
            }
        }
    }

    suspend fun add(id: Long) {
        dataStore.updateData { settings ->
            if (id in settings.trackedOrderIds) {
                settings
            } else {
                settings.copy(trackedOrderIds = (settings.trackedOrderIds + id).sorted())
            }
        }
    }
}
