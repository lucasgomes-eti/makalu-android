package eti.lucasgomes.features.menuItem.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eti.lucasgomes.features.menuItem.ui.model.MenuItemAction
import eti.lucasgomes.features.menuItem.ui.model.MenuItemUiState
import eti.lucasgomes.makalu.components.CardItem
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition

@Composable
internal fun BoxScope.MenuItemScreen(
    uiState: MenuItemUiState,
    onAction: (MenuItemAction) -> Unit
) {
    ConfigureTopBar("Menu item id: ${uiState.id}")
    OnFirstComposition { onAction(MenuItemAction.OnInitialFetch) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CardItem(headlineContent = {
            Text(text = "Ingredients")
        })
    }
}