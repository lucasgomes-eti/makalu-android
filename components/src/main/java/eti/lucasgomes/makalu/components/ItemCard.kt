package eti.lucasgomes.makalu.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
    val currency = stringResource(R.string.currency_symbol)
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
                        "$currency $price",
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
                    modifier = Modifier.fillMaxWidth(),
                    contentWeight = 1f,
                    headlineContent = {
                        configurations.forEach { configuration ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                configuration.name,
                                style = typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
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
                                        "+$currency ${option.additionalPrice}",
                                        style = typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            if (configurations.last() != configuration) {
                                HorizontalDivider()
                            }
                        }
                    },
                    supportingContent = {
                        Text(notes ?: "", style = typography.bodyMedium)
                    }
                )
            }
            footer()
        }
    }
}
