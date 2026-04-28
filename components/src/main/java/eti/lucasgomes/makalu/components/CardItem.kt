package eti.lucasgomes.makalu.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CardItem(
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
    leadingContent: @Composable () -> Unit = {},
    ) {
    SurfaceWithOptionalClick(
        onClick = onClick
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.size(0.dp))
            trailingContent()
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                headlineContent()
                supportingContent()
            }
            leadingContent()
            Spacer(Modifier.size(0.dp))
        }
    }
}

@Composable
private fun SurfaceWithOptionalClick(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    if (onClick == null) {
        Surface(
            border = BorderStroke(1.dp, colorScheme.outline),
            shape = RoundedCornerShape(12.dp),
        ) {
            content()
        }
    } else {
        Surface(
            border = BorderStroke(1.dp, colorScheme.outline),
            shape = RoundedCornerShape(12.dp),
            onClick = onClick
        ) {
            content()
        }
    }
}

@Composable
@Preview()
private fun CardItemPreview() {
    CardItem(modifier = Modifier.height(80.dp), headlineContent = {
        Text("Delivery time", maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("60 min", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }, onClick = {})
}