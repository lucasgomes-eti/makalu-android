package eti.lucasgomes.features.store.ui.model

internal sealed interface StoreAction {
    data object OnInitialFetch : StoreAction
}