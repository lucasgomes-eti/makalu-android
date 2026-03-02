package eti.lucasgomes.makalu.features.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState

@Composable
internal fun LazyItemScope.StoreListItem(store: StoreUiState.Data, onStoreClicked: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .animateItem(),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        onClick = onStoreClicked
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.size(0.dp))
            AsyncImage(
                modifier = Modifier.size(40.dp).clip(CircleShape),
                model = store.logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(store.name, style = typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(store.category, style = typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            AsyncImage(
                modifier = Modifier.size(80.dp),
                model = store.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
@Preview
private fun StoreListItemPreview() {
    LazyColumn {
        item {
            StoreListItem(StoreUiState.Data(
                1,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                "Ut placerat feugiat nisi, a maximus turpis varius in. Maecenas at auctor ipsum. Suspendisse potenti. Donec tincidunt magna sit amet massa convallis, id pretium mauris placerat.",
                "",
                ""
            )) {}
        }
    }

}