package eti.lucasgomes.makalu.features.home.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.features.home.R

@Composable
internal fun LazyItemScope.LoadingStoresListItem() {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .animateItem(),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Column {
            Spacer(Modifier.Companion.height(120.dp))
            Text(stringResource(R.string.loading_stores_in_your_area))
        }
    }
}