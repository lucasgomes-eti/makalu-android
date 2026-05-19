package eti.lucasgomes.makalu.features.home.ui.model

import eti.lucasgomes.makalu.shared.mapImageUrl
import java.util.UUID

internal sealed class StoreUiState(val key: String) {
    data class Data(
        val id: Long,
        val name: String,
        val category: String,
        private val logoId: Long?,
        private val coverId: Long?,
    ) : StoreUiState(id.toString()) {
        val logoUrl: String?
            get() = mapImageUrl(logoId)

        val coverUrl: String?
            get() = mapImageUrl(coverId)
    }

    data object Loading : StoreUiState(UUID.randomUUID().toString())

    data object NoContent : StoreUiState(UUID.randomUUID().toString())
}