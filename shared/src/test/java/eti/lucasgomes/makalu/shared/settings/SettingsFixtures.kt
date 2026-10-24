package eti.lucasgomes.makalu.shared.settings

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsDataStore(
    initial: Settings = Settings(),
    private val updateGate: CompletableDeferred<Unit>? = null
) : DataStore<Settings> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Settings> = state

    val current: Settings get() = state.value

    override suspend fun updateData(transform: suspend (t: Settings) -> Settings): Settings {
        updateGate?.await()
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
