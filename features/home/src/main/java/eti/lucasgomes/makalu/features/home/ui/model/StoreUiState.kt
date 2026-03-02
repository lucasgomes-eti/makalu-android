package eti.lucasgomes.makalu.features.home.ui.model

import java.util.UUID

internal sealed class StoreUiState(val key: String) {
    data class Data(
        val id: Long,
        val name: String,
        val category: String,
        val logoUrl: String?,
        val coverUrl: String?,
    ) : StoreUiState(id.toString())

    data object Loading : StoreUiState(UUID.randomUUID().toString())

    data object NoContent : StoreUiState(UUID.randomUUID().toString())
}