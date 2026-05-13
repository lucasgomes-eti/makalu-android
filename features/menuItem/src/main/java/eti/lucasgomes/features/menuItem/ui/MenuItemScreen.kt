package eti.lucasgomes.features.menuItem.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import eti.lucasgomes.features.menuItem.R
import eti.lucasgomes.features.menuItem.ui.model.ConfigurationUiState
import eti.lucasgomes.features.menuItem.ui.model.MenuItemAction
import eti.lucasgomes.features.menuItem.ui.model.MenuItemUiState
import eti.lucasgomes.features.menuItem.ui.model.OptionUiState
import eti.lucasgomes.makalu.components.CardItem
import eti.lucasgomes.makalu.components.appBars.ExpandableTopAppBar
import eti.lucasgomes.makalu.components.banners.ErrorBanner
import eti.lucasgomes.makalu.components.buttons.ConfigureFab
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText
import java.math.BigDecimal

@Composable
internal fun BoxScope.MenuItemScreen(
    uiState: MenuItemUiState, onAction: (MenuItemAction) -> Unit
) {
    OnFirstComposition { onAction(MenuItemAction.OnInitialFetch) }
    ConfigureFab(
        icon = painterResource(R.drawable.add_shopping_cart),
        contentDescription = "Add do cart icon"
    ) { onAction(MenuItemAction.AddToCartClicked) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            ExpandableTopAppBar(
                scrollBehavior,
                backgroundImageUrl = uiState.imageUrl,
                title = if (uiState.isLoading) stringResource(eti.lucasgomes.makalu.components.R.string.loading) else uiState.name.asString()
            ) { onAction(MenuItemAction.NavigateBackClicked) }
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding + PaddingValues(0.dp, 16.dp, 0.dp, 72.dp)
        ) {
            if (uiState.generalError != UiText.Empty) {
                item {
                    ErrorBanner(uiState.generalError.asString()) {
                        onAction(MenuItemAction.OnDismissError)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            if (uiState.isLoading) {
                item {
                    Row(
                        Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                    ) { LoadingIndicator() }
                    Spacer(Modifier.height(16.dp))
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
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
            uiState.configurations.forEach { (config, options) ->
                item {
                    Text(
                        config.name,
                        style = typography.labelLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                when (config.type) {
                    ConfigurationUiState.Type.SINGLE_CHOICE -> {
                        item {
                            SingleChoiceSection(options) {
                                onAction(
                                    MenuItemAction.OptionSelected(
                                        config,
                                        options.indexOf(it)
                                    )
                                )
                            }
                        }
                    }

                    ConfigurationUiState.Type.MULTIPLE_CHOICE -> {
                        item {
                            MultipleChoiceSection(options) {
                                onAction(
                                    MenuItemAction.OptionSelected(
                                        config,
                                        options.indexOf(it)
                                    )
                                )
                            }
                        }
                    }

                    ConfigurationUiState.Type.QUANTITY -> {
                        items(options) { option ->
                            if (option is OptionUiState.Quantity) {
                                QuantityItem(
                                    label = option.label,
                                    additionalPrice = option.additionalPrice,
                                    amount = option.amount,
                                    onAmountChanged = {
                                        onAction(
                                            MenuItemAction.QuantityChanged(
                                                config,
                                                options.indexOf(option),
                                                it
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = uiState.notes,
                    onValueChange = { onAction(MenuItemAction.NotesChanged(it)) },
                    label = {
                        Text("Notes")
                    })

            }
        }
    }
}

@Composable
private fun SingleChoiceSection(
    options: List<OptionUiState>,
    onOptionSelected: (OptionUiState.SingleChoice) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        options.forEach { option ->
            if (option is OptionUiState.SingleChoice) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, colorScheme.outline), RoundedCornerShape(12.dp))
                        .selectable(
                            selected = option.isSelected,
                            role = Role.RadioButton,
                            onClick = { onOptionSelected(option) }
                        )
                        .padding(16.dp)
                ) {
                    RadioButton(selected = option.isSelected, onClick = null)
                    Column {
                        Text(option.label, style = typography.titleMedium)
                        Text(
                            "+${stringResource(eti.lucasgomes.makalu.components.R.string.currency_symbol)}${option.additionalPrice}",
                            style = typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceSection(
    options: List<OptionUiState>,
    onOptionSelected: (OptionUiState.MultipleChoice) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        options.forEach { option ->
            if (option is OptionUiState.MultipleChoice) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, colorScheme.outline), RoundedCornerShape(12.dp))
                        .selectable(
                            selected = option.isSelected,
                            role = Role.Checkbox,
                            onClick = { onOptionSelected(option) }
                        )
                        .padding(16.dp)
                ) {
                    Checkbox(checked = option.isSelected, onCheckedChange = null)
                    Column {
                        Text(option.label, style = typography.titleMedium)
                        Text(
                            "+${stringResource(eti.lucasgomes.makalu.components.R.string.currency_symbol)}${option.additionalPrice}",
                            style = typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.QuantityItem(
    label: String,
    additionalPrice: BigDecimal,
    amount: Int,
    onAmountChanged: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        CardItem(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentWeight = 1f,
            headlineContent = {
                Text(label, style = typography.titleMedium)
            },
            supportingContent = {
                Text(
                    "+${stringResource(eti.lucasgomes.makalu.components.R.string.currency_symbol)}$additionalPrice",
                    style = typography.labelMedium
                )
            },
            trailingContent = {
                Spacer(Modifier)
                AnimatedVisibility(amount > 0) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(colorScheme.primary, shape = CircleShape)
                    ) {
                        AnimatedContent(
                            amount,
                            modifier = Modifier.align(Alignment.Center),
                        ) {
                            Text(
                                "$it",
                                style = typography.titleMedium,
                                color = colorScheme.onPrimary,
                            )
                        }
                    }
                }
            },
            leadingContent = {
                AnimatedVisibility(amount > 0) {
                    OutlinedButton(onClick = { onAmountChanged(amount - 1) }) {
                        Text("-")
                    }
                }
                Button(onClick = { onAmountChanged(amount + 1) }) {
                    Text("+")
                }
                Spacer(Modifier)
            }
        )
        Spacer(Modifier.padding(8.dp))
    }
}