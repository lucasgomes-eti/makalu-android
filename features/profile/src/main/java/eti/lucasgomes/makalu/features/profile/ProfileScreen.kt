package eti.lucasgomes.makalu.features.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.banners.ErrorBanner
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ProfileScreen(uiState: ProfileUiState, onAction: (ProfileAction) -> Unit) {

    OnFirstComposition { onAction(ProfileAction.InitialFetch) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(uiState.isLoading) { LoadingIndicator() }
        AnimatedVisibility(uiState.generalError != UiText.Empty) {
            ErrorBanner(uiState.generalError.asString()) { onAction(ProfileAction.ErrorDismiss) }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(ProfileAction.LogoutClicked) }) {
            Text(text = stringResource(R.string.logout))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    ProfileScreen(ProfileUiState()) {}
}