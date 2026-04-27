package eti.lucasgomes.features.store.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.features.store.model.MenuItemResponse
import eti.lucasgomes.features.store.ui.model.StoreAction
import eti.lucasgomes.features.store.ui.model.StoreUiState
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.appBars.LocalTopBarUiController
import eti.lucasgomes.makalu.components.appBars.TopBarUiController
import eti.lucasgomes.makalu.components.banners.ErrorBanner
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BoxScope.StoreScreen(uiState: StoreUiState, onAction: (StoreAction) -> Unit) {
    OnFirstComposition { onAction(StoreAction.OnInitialFetch) }
    ConfigureTopBar(uiState.name.asString())
    AnimatedVisibility(uiState.generalError != UiText.Empty) {
        ErrorBanner(uiState.generalError.asString()) {
            onAction(StoreAction.OnDismissError)
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        uiState.menuItems.forEach { (category, items) ->
            item {
                Text(
                    category,
                    style = typography.labelLarge
                )
            }

            items(items) { MenuListItem(it) {} }
        }
    }
}

@Composable
private fun LazyItemScope.MenuListItem(item: MenuItemResponse, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .animateItem(),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.size(0.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    item.name,
                    style = typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "R$ ${item.price}",
                    style = typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun StoreScreenPreview() {
    val topBarUiController = remember { TopBarUiController() }
    CompositionLocalProvider(
        LocalTopBarUiController provides topBarUiController,
    ) {
        Box {
            StoreScreen(
                StoreUiState(
                    1,
                    menuItems = mapOf(
                        "Pizza" to listOf(
                            MenuItemResponse(
                                1,
                                1,
                                "Pizza",
                                "Margerita",
                                10.0.toBigDecimal(),
                                null,
                                emptyList()
                            )
                        )
                    )
                )
            ) { }
        }
    }

}