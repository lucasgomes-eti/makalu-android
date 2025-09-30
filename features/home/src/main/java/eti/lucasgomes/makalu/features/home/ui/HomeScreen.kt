package eti.lucasgomes.makalu.features.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.ConfigureFab
import eti.lucasgomes.makalu.components.OnFirstComposition
import eti.lucasgomes.makalu.features.home.R
import eti.lucasgomes.makalu.features.home.ui.components.LoadingStoresListItem
import eti.lucasgomes.makalu.features.home.ui.components.NoStoresListItem
import eti.lucasgomes.makalu.features.home.ui.components.RequireAuthDialog
import eti.lucasgomes.makalu.features.home.ui.components.StoreListItem
import eti.lucasgomes.makalu.features.home.ui.model.FilterUiState
import eti.lucasgomes.makalu.features.home.ui.model.HomeAction
import eti.lucasgomes.makalu.features.home.ui.model.HomeUiState
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState

@Composable
internal fun HomeScreen(uiState: HomeUiState, onAction: (HomeAction) -> Unit) {
    OnFirstComposition {
        onAction(HomeAction.InitialFetch)
    }
    ConfigureFab(icon = painterResource(R.drawable.search), contentDescription = "Search Store") { }
    if (uiState.isAuthDialogVisible) {
        RequireAuthDialog(
            onDismissRequest = { onAction(HomeAction.AuthDialogDismissed) },
            onConfirmation = { onAction(HomeAction.AuthClicked) }
        )
    }
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(PaddingValues(16.dp, 16.dp, 16.dp, 0.dp)),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {}) {
                AnimatedContent(
                    targetState = uiState.address
                ) { value ->
                    Text(value)
                }
                Icon(painterResource(R.drawable.arrow_right), "Arrow Right")
            }
            FilledTonalIconButton(onClick = {}) {
                Icon(painterResource(R.drawable.sort), "Sort")
            }
        }
        Column(Modifier.height(32.dp)) {
            AnimatedVisibility(
                uiState.isFiltersLoading,
                enter = fadeIn() + slideIn { IntOffset(0, -it.height) },
                exit = slideOut(
                    tween(
                        delayMillis = 0,
                    )
                ) { IntOffset(0, -it.height) } + fadeOut(),
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            AnimatedVisibility(
                uiState.isFiltersLoading.not(),
                enter = fadeIn() + slideIn(
                    tween(
                        delayMillis = 300,
                    )
                ) { IntOffset(it.width, 0) },
                exit = slideOut { IntOffset(it.width, 0) } + fadeOut(),
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.filters) { category ->
                        FilterChip(
                            selected = category.isSelected,
                            label = { Text(category.label) },
                            onClick = {})
                    }
                }
            }
        }

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = uiState.isStoresLoading,
            onRefresh = { onAction(HomeAction.RefreshStores) }
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 79.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.stores,
                    key = { it.key },
                ) { store ->
                    when (store) {
                        is StoreUiState.Data -> StoreListItem(store) { onAction(HomeAction.StoreClicked) }
                        StoreUiState.Loading -> LoadingStoresListItem()
                        StoreUiState.NoContent -> NoStoresListItem()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    HomeScreen(
        HomeUiState(
            address = "123 Main St",
            filters = listOf(
                FilterUiState("All", true),
                FilterUiState("Coffee", false),
                FilterUiState("Pizza", false),
            ),
            stores = listOf(
                StoreUiState.Data(
                    name = "Scary's",
                    category = "Burger",
                    logoUrl = "https://picsum.photos/200",
                    coverUrl = "https://picsum.photos/200/300",
                )
            )
        )
    ) {}
}