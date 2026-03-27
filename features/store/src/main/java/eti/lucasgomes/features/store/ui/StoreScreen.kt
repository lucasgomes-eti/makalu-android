package eti.lucasgomes.features.store.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eti.lucasgomes.features.store.ui.model.StoreAction
import eti.lucasgomes.features.store.ui.model.StoreUiState
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition

@Composable
internal fun BoxScope.StoreScreen(uiState: StoreUiState, onAction: (StoreAction) -> Unit) {
    OnFirstComposition { onAction(StoreAction.OnInitialFetch) }
    ConfigureTopBar(uiState.name.asString())
    Text("id: ${uiState.id}", modifier = Modifier.align(Alignment.Center))
}