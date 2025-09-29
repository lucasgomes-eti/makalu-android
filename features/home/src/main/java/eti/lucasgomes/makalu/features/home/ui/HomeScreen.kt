package eti.lucasgomes.makalu.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.ConfigureFab
import eti.lucasgomes.makalu.features.home.R
import eti.lucasgomes.makalu.features.home.ui.components.StoreListItem
import eti.lucasgomes.makalu.features.home.ui.model.CategoryUiState
import eti.lucasgomes.makalu.features.home.ui.model.HomeAction
import eti.lucasgomes.makalu.features.home.ui.model.HomeUiState
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState

@Composable
internal fun HomeScreen(uiState: HomeUiState, onAction: (HomeAction) -> Unit) {
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
                .padding(
                    PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 0.dp
                    )
                ),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {}) {
                Text(uiState.address)
                Icon(painterResource(R.drawable.arrow_right), "Arrow Right")
            }
            FilledTonalIconButton(onClick = {}) {
                Icon(painterResource(R.drawable.sort), "Sort")
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(uiState.categories) { category ->
                FilterChip(
                    selected = category.isSelected,
                    label = { Text(category.label) },
                    onClick = {})
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(
                16.dp,
                0.dp,
                16.dp,
                79.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.stores) { store ->
                StoreListItem(store) { onAction(HomeAction.StoreClicked) }
            }
        }
    }
}

@Composable
private fun RequireAuthDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = { Icon(painterResource(R.drawable.login), "Login Icon") },
        title = { Text("Login Required") },
        text = { Text("You must be logged in to order.") },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmation) {
                Text("Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Dismiss")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    HomeScreen(
        HomeUiState(
            address = "123 Main St",
            categories = listOf(
                CategoryUiState("All", true),
                CategoryUiState("Coffee", false),
                CategoryUiState("Pizza", false),
            ),
            stores = listOf(
                StoreUiState(
                    name = "Scary's",
                    category = "Burger",
                    logoUrl = "https://picsum.photos/200",
                    coverUrl = "https://picsum.photos/200/300",
                )
            )
        )
    ) {}
}