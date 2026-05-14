package eti.lucasgomes.features.cart.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eti.lucasgomes.features.cart.R
import eti.lucasgomes.makalu.components.CardItem
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.buttons.ExpressiveButton
import eti.lucasgomes.makalu.components.buttons.ExpressiveTextButton
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition

@Composable
internal fun BoxScope.CartScreen(uiState: CartUiState, onAction: (CartAction) -> Unit) {
    OnFirstComposition { onAction(CartAction.OnInitialFetch) }
    ConfigureTopBar("Cart")

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Items", style = typography.titleLarge) }
        items(uiState.items) { CartItem(it) }
        item { Text("Address", style = typography.titleLarge) }
        item {
            CardItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                headlineContent = {
                    Text("uiState.address", style = typography.bodyLarge)
                },
                trailingContent = {
                    Row {
                        Spacer(Modifier.width(16.dp))
                        Icon(
                            painterResource(R.drawable.location),
                            contentDescription = "Location icon",
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE1F5FE), CircleShape)
                                .padding(8.dp),
                            tint = Color(0xFF01579B),
                        )
                    }
                }
            )
        }
        item { Text("Total", style = typography.titleLarge) }
        item {
            CardItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                headlineContent = {
                    Text("uiState.price", style = typography.bodyLarge)
                },
                trailingContent = {
                    Row {
                        Spacer(Modifier.width(16.dp))
                        Icon(
                            painterResource(R.drawable.money),
                            contentDescription = "Money icon",
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE8F5E9), CircleShape)
                                .padding(8.dp),
                            tint = Color(0xFF1B5E20),
                        )
                    }
                }
            )
        }
        item {
            ExpressiveButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {}
            ) {
                Text("Make order")
            }
        }
    }


}

@Composable
private fun LazyItemScope.CartItem(uiState: CartItemUiState) {
    Card {
        Column(modifier = Modifier.padding(8.dp)) {
            CardItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .animateItem(),
                contentWeight = 1f,
                headlineContent = {
                    Text(
                        text = "item.name",
                        style = typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        "${stringResource(eti.lucasgomes.makalu.components.R.string.currency_symbol)} ",
                        style = typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    AsyncImage(
                        modifier = Modifier.size(80.dp),
                        model = "item.imageUrl",
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            )
            ExpressiveTextButton(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error),
                onClick = {}
            ) {
                Text("Remove")
            }
        }
    }
}