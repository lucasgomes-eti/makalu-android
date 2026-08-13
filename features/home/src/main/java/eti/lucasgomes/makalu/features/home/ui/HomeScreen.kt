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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.ExpressivePullToRefreshIndicator
import eti.lucasgomes.makalu.components.buttons.ConfigureFab
import eti.lucasgomes.makalu.components.buttons.ExpressiveTextButton
import eti.lucasgomes.makalu.components.buttons.FabUiController
import eti.lucasgomes.makalu.components.buttons.LocalFabUiController
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.home.R
import eti.lucasgomes.makalu.features.home.ui.components.LoadingStoresListItem
import eti.lucasgomes.makalu.features.home.ui.components.NoStoresListItem
import eti.lucasgomes.makalu.features.home.ui.components.RequireAuthDialog
import eti.lucasgomes.makalu.features.home.ui.components.StoreListItem
import eti.lucasgomes.makalu.features.home.ui.model.CategoryUiState
import eti.lucasgomes.makalu.features.home.ui.model.HomeAction
import eti.lucasgomes.makalu.features.home.ui.model.HomeUiState
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeScreen(uiState: HomeUiState, onAction: (HomeAction) -> Unit) {
    OnFirstComposition {
        onAction(HomeAction.InitialFetch)
    }
    ConfigureFab(
        icon = painterResource(R.drawable.search),
        contentDescription = stringResource(R.string.accessibility_search_store)
    ) { }
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
            ExpressiveTextButton(
                onClick = { onAction(HomeAction.AddressClicked) },
                modifier = Modifier.widthIn(
                    ButtonDefaults.MinWidth,
                    with(LocalDensity.current) {
                        LocalWindowInfo.current.containerSize.width.toDp() - ButtonDefaults.MinWidth - 32.dp
                    }
                )
            ) {
                Row(modifier = Modifier.width(IntrinsicSize.Max)) {
                    val loadingAddressText = UiText.StringResource(R.string.loading_address)
                    var address by remember { mutableStateOf(uiState.address) }
                    LaunchedEffect(uiState.isAddressLoading) {
                        address = if (uiState.isAddressLoading) {
                            loadingAddressText
                        } else {
                            uiState.address
                        }
                    }
                    AnimatedContent(
                        modifier = Modifier.weight(1f),
                        targetState = address.asString()
                    ) { value ->
                        Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(
                        painterResource(R.drawable.arrow_right),
                        stringResource(R.string.accessibility_arrow_right_icon)
                    )
                }
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
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
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
                    itemsIndexed(
                        uiState.categories,
                        key = { _, category -> category.id }) { i, category ->
                        FilterChip(
                            selected = category.isSelected,
                            label = { Text(category.label.asString()) },
                            onClick = { onAction(HomeAction.CategoryClicked(i)) },
                        )
                    }
                }
            }
        }

        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = uiState.isStoresLoading,
            state = pullToRefreshState,
            onRefresh = { onAction(HomeAction.RefreshStores) },
            indicator = {
                ExpressivePullToRefreshIndicator(
                    pullToRefreshState,
                    uiState.isStoresLoading
                )
            }
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
                        is StoreUiState.Data -> StoreListItem(store) { onAction(HomeAction.StoreClicked(store.id)) }
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

    CompositionLocalProvider(LocalFabUiController provides FabUiController()) {
        HomeScreen(
            HomeUiState(
                address = UiText.PlainText("123 Main St"),
                categories = listOf(
                    CategoryUiState(1, UiText.PlainText("All"), true),
                    CategoryUiState(2, UiText.PlainText("Coffee"), false),
                    CategoryUiState(3, UiText.PlainText("Pizza"), false),
                ),
                stores = listOf(
                    StoreUiState.Data(
                        id = 1,
                        name = "Scary's",
                        category = "Burger",
                        logoId = 1,
                        coverId = 1,
                    )
                )
            )
        ) {}
    }

}