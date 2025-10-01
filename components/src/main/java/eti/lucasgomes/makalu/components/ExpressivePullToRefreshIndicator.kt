package eti.lucasgomes.makalu.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun BoxScope.ExpressivePullToRefreshIndicator(
    pullToRefreshState: PullToRefreshState,
    isRefreshing: Boolean,
) {
    PullToRefreshDefaults.IndicatorBox(
        modifier = Modifier.Companion
            .size(48.dp)
            .align(Alignment.Companion.TopCenter),
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        containerColor = PullToRefreshDefaults.loadingIndicatorContainerColor,
        elevation = 0.dp
    ) {
        Crossfade(
            targetState = isRefreshing,
        ) { refreshing ->
            if (refreshing) {
                LoadingIndicator(
                    modifier = Modifier.Companion.size(38.dp),
                    color = PullToRefreshDefaults.loadingIndicatorColor
                )
            } else {
                LoadingIndicator(
                    modifier = Modifier.Companion.size(38.dp),
                    progress = { pullToRefreshState.distanceFraction },
                    color = PullToRefreshDefaults.loadingIndicatorColor
                )
            }
        }
    }
}