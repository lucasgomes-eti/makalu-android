package eti.lucasgomes.features.store.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eti.lucasgomes.features.store.R
import eti.lucasgomes.features.store.ui.model.MenuItemUiState
import eti.lucasgomes.features.store.ui.model.StoreAction
import eti.lucasgomes.features.store.ui.model.StoreUiState
import eti.lucasgomes.makalu.components.CardItem
import eti.lucasgomes.makalu.components.appBars.ExpandableTopAppBar
import eti.lucasgomes.makalu.components.appBars.LocalTopBarUiController
import eti.lucasgomes.makalu.components.appBars.TopBarUiController
import eti.lucasgomes.makalu.components.banners.ErrorBanner
import eti.lucasgomes.makalu.components.buttons.ConfigureFab
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun BoxScope.StoreScreen(uiState: StoreUiState, onAction: (StoreAction) -> Unit) {
    OnFirstComposition { onAction(StoreAction.OnInitialFetch) }

    ConfigureFab(painterResource(R.drawable.shopping_cart), "Go to cart button") {
        onAction(StoreAction.CartClicked)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExpandableTopAppBar(
                scrollBehavior,
                backgroundImageUrl = uiState.coverImageUrl,
                if (uiState.isLoading) stringResource(eti.lucasgomes.makalu.components.R.string.loading) else uiState.name.asString()
            ) { onAction(StoreAction.NavigateBackClicked) }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (uiState.generalError != UiText.Empty) {
                item {
                    ErrorBanner(uiState.generalError.asString()) {
                        onAction(StoreAction.OnDismissError)
                    }
                }
            }
            if (uiState.isLoading) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) { LoadingIndicator() }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CardItem(
                        Modifier.height(80.dp),
                        headlineContent = {
                            Text(
                                stringResource(R.string.delivery_fee),
                                style = typography.bodyMedium
                            )
                        },
                        supportingContent = {
                            Text(
                                "${stringResource(eti.lucasgomes.makalu.components.R.string.currency_symbol)} ${uiState.deliveryFee}",
                                style = typography.titleMedium
                            )
                        },
                    )
                }
            }
            uiState.menuItems.forEach { (category, items) ->
                item {
                    Text(
                        category,
                        style = typography.labelLarge
                    )
                }

                items(items) { MenuListItem(it) { onAction(StoreAction.MenuItemClicked(it.id)) } }
            }
        }
    }
}

@Composable
private fun LazyItemScope.MenuListItem(item: MenuItemUiState, onClick: () -> Unit) {
    CardItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .animateItem(),
        contentWeight = 1f,
        onClick = onClick,
        headlineContent = {
            Text(
                item.name,
                style = typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                "${stringResource(eti.lucasgomes.makalu.components.R.string.currency_symbol)} ${item.price}",
                style = typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            AsyncImage(
                modifier = Modifier.size(80.dp),
                model = item.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    )
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
                            MenuItemUiState(
                                1,
                                "Pizza",
                                "Margerita",
                                10.0.toBigDecimal(),
                            )
                        )
                    )
                )
            ) { }
        }
    }

}