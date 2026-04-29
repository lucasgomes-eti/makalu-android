package eti.lucasgomes.features.menuItem.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eti.lucasgomes.features.menuItem.R
import eti.lucasgomes.features.menuItem.ui.model.ConfigurationUiState
import eti.lucasgomes.features.menuItem.ui.model.MenuItemAction
import eti.lucasgomes.features.menuItem.ui.model.MenuItemUiState
import eti.lucasgomes.makalu.components.CardItem
import eti.lucasgomes.makalu.components.appBars.ExpandableTopAppBar
import eti.lucasgomes.makalu.components.banners.ErrorBanner
import eti.lucasgomes.makalu.components.buttons.ConfigureFab
import eti.lucasgomes.makalu.components.buttons.ExpressiveButton
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText

@Composable
internal fun BoxScope.MenuItemScreen(
    uiState: MenuItemUiState,
    onAction: (MenuItemAction) -> Unit
) {
    OnFirstComposition { onAction(MenuItemAction.OnInitialFetch) }
    ConfigureFab(
        icon = painterResource(R.drawable.add_shopping_cart),
        contentDescription = "Add do cart icon"
    ) { }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExpandableTopAppBar(
                scrollBehavior,
                backgroundImageUrl = uiState.imageUrl,
                title = if (uiState.isLoading) stringResource(eti.lucasgomes.makalu.components.R.string.loading) else uiState.name.asString()
            ) { onAction(MenuItemAction.NavigateBackClicked) }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = innerPadding + PaddingValues(16.dp, 16.dp, 16.dp, 72.dp)
        ) {
            if (uiState.generalError != UiText.Empty) {
                item {
                    ErrorBanner(uiState.generalError.asString()) {
                        onAction(MenuItemAction.OnDismissError)
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
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CardItem(headlineContent = {
                        Text(text = "Price", style = typography.bodyMedium)
                    }, supportingContent = {
                        Text(
                            "${stringResource(eti.lucasgomes.makalu.components.R.string.currency_symbol)} ${uiState.price}",
                            style = typography.titleMedium
                        )
                    })
                    uiState.ingredients?.let { ingredients ->
                        CardItem(headlineContent = {
                            Text(
                                text = stringResource(R.string.ingredients),
                                style = typography.bodyMedium
                            )
                        }, supportingContent = {
                            Text(ingredients, style = typography.titleMedium)
                        })
                    }
                }
            }
            items(uiState.configurations) { configuration ->
                Text(configuration.name)
                when (configuration.type) {
                    ConfigurationUiState.Type.SINGLE_CHOICE -> {
                        configuration.options.forEach { opt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(selected = false, onClick = {})
                                Text(opt)
                            }
                        }
                    }

                    ConfigurationUiState.Type.MULTIPLE_CHOICE -> {
                        configuration.options.forEach { opt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(checked = false, onCheckedChange = {})
                                Text(opt)
                            }

                        }
                    }

                    ConfigurationUiState.Type.QUANTITY -> {
                        configuration.options.forEach { opt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(opt)
                                ExpressiveButton(onClick = {}) {
                                    Text("+")
                                }
                                Text("count")
                                ExpressiveButton(onClick = {}) {
                                    Text("-")
                                }
                            }
                        }
                    }
                }
            }
            item {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = "",
                    onValueChange = {},
                    label = {
                        Text("Notes")
                    }
                )
            }
        }
    }
}