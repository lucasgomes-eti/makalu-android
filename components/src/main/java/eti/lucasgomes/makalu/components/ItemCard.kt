package eti.lucasgomes.makalu.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eti.lucasgomes.makalu.components.format.formatMoney
import eti.lucasgomes.makalu.components.format.formatMoneyAddition
import java.math.BigDecimal

@Composable
fun ItemCard(
    name: String,
    imageUrl: String?,
    price: BigDecimal,
    notes: String?,
    configurations: List<ItemConfigurationUiState>,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    Card(modifier) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentWeight = 1f,
                headlineContent = {
                    Text(
                        text = name,
                        style = typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        formatMoney(price),
                        style = typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    AsyncImage(
                        modifier = Modifier.size(80.dp),
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            )
            if (configurations.isNotEmpty()) {
                CardItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentWeight = 1f,
                    headlineContent = {
                        ItemCardSectionLabel(stringResource(R.string.item_options))
                    },
                    supportingContent = {
                        configurations.forEachIndexed { index, configuration ->
                            if (index > 0) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Text(
                                configuration.name,
                                style = typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            configuration.options.forEach { option ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        option.quantity.toString(),
                                        style = typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        option.name,
                                        style = typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        formatMoneyAddition(option.additionalPrice),
                                        style = typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                )
            }
            if (!notes.isNullOrBlank()) {
                CardItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentWeight = 1f,
                    headlineContent = {
                        ItemCardSectionLabel(stringResource(R.string.item_notes))
                    },
                    supportingContent = {
                        Text(notes.trim(), style = typography.bodyMedium)
                    }
                )
            }
            footer()
        }
    }
}

@Composable
private fun ItemCardSectionLabel(text: String) {
    Text(text, style = typography.labelLarge, color = colorScheme.primary)
}

private val previewConfigurations = listOf(
    ItemConfigurationUiState(
        id = 1,
        name = "Sauces",
        options = listOf(
            ItemOptionUiState(id = 1, name = "Teriyaki", additionalPrice = BigDecimal("1.50"), quantity = 2),
            ItemOptionUiState(id = 2, name = "Spicy mayo", additionalPrice = BigDecimal("1.00"), quantity = 1)
        )
    ),
    ItemConfigurationUiState(
        id = 2,
        name = "Extras",
        options = listOf(
            ItemOptionUiState(id = 3, name = "Cream cheese", additionalPrice = BigDecimal("2.00"), quantity = 1)
        )
    )
)

@Preview(showBackground = true)
@Composable
private fun ItemCardOptionsAndNotesPreview() {
    ItemCard(
        name = "Hot Filadélfia",
        imageUrl = null,
        price = BigDecimal("22.11"),
        notes = "No onions",
        configurations = previewConfigurations
    )
}

@Preview(showBackground = true)
@Composable
private fun ItemCardOptionsOnlyPreview() {
    ItemCard(
        name = "Hot Filadélfia",
        imageUrl = null,
        price = BigDecimal("22.11"),
        notes = null,
        configurations = previewConfigurations.take(1)
    )
}

@Preview(showBackground = true)
@Composable
private fun ItemCardNotesOnlyPreview() {
    ItemCard(
        name = "Hot Filadélfia",
        imageUrl = null,
        price = BigDecimal("22.11"),
        notes = "No onions",
        configurations = emptyList()
    )
}

@Preview(showBackground = true)
@Composable
private fun ItemCardPlainPreview() {
    ItemCard(
        name = "Hot Filadélfia",
        imageUrl = null,
        price = BigDecimal("22.11"),
        notes = "   ",
        configurations = emptyList()
    )
}

@Preview(showBackground = true)
@Composable
private fun ItemCardWithFooterPreview() {
    ItemCard(
        name = "Hot Filadélfia",
        imageUrl = null,
        price = BigDecimal("22.11"),
        notes = "No onions",
        configurations = previewConfigurations.take(1)
    ) {
        TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Remove", color = colorScheme.error)
        }
    }
}
