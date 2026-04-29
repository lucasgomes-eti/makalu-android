package eti.lucasgomes.makalu.components.appBars

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ExpandableTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    backgroundImageUrl: String? = null,
    title: String,
    onNavigateBack: () -> Unit
) {

    val maxHeight = 152.dp
    val minHeight = 64.dp
    val collapseFraction = scrollBehavior.state.collapsedFraction
    val height = maxHeight - (maxHeight - minHeight) * collapseFraction
    val isCollapsed = collapseFraction == 1f
    val topAppBarTitleContentColor by animateColorAsState(
        if (isCollapsed) colorScheme.contentColorFor(
            colorScheme.surfaceContainer
        ) else Color.White
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        AsyncImage(
            model = backgroundImageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.8f),
                        )
                    )
                )
        )
        LargeTopAppBar(
            title = {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            expandedHeight = maxHeight,
            collapsedHeight = minHeight,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = topAppBarTitleContentColor
            ),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painterResource(eti.lucasgomes.makalu.components.R.drawable.arrow_back),
                        stringResource(eti.lucasgomes.makalu.components.R.string.accessibility_back_button),
                        tint = topAppBarTitleContentColor
                    )
                }
            },
        )
    }
}