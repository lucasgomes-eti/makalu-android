package eti.lucasgomes.makalu.features.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eti.lucasgomes.makalu.features.home.ui.model.StoreUiState

@Composable
internal fun StoreListItem(store: StoreUiState) {
    Surface(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(80.dp),
        border = BorderStroke(1.dp, colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        onClick = {}
    ) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.Companion.size(0.dp))
            AsyncImage(
                modifier = Modifier.Companion.size(40.dp),
                model = store.logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Companion.Crop
            )
            Column(
                modifier = Modifier.Companion.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(store.name, style = typography.titleMedium)
                Text(store.category, style = typography.labelMedium)
            }
            AsyncImage(
                modifier = Modifier.Companion.size(80.dp),
                model = store.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Companion.Crop
            )
        }
    }
}