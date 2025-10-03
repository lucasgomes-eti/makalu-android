package eti.lucasgomes.makalu.features.home.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.features.home.R

@Composable
internal fun LazyItemScope.NoStoresListItem() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .animateItem(),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Column {
            Icon(
                painterResource(R.drawable.store),
                contentDescription = stringResource(R.string.accessibility_store_image),
                modifier = Modifier.Companion.size(128.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(stringResource(R.string.no_stores_found))
        }
    }
}