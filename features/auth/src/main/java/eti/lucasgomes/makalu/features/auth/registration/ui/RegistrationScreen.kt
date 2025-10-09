package eti.lucasgomes.makalu.features.auth.registration.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.features.auth.R
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationAction
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationUiState

@Composable
internal fun RegistrationScreen(
    uiState: RegistrationUiState,
    onAction: (RegistrationAction) -> Unit
) {
    ConfigureTopBar(
        title = stringResource(R.string.registration),
        navigationActions = listOf(),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(eti.lucasgomes.makalu.components.R.drawable.profile_pic),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary)
                    .clickable(
                        role = Role.Image,
                        onClick = { onAction(RegistrationAction.ProfileImageClicked) })
            )
        }
        TextField(
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            value = uiState.email,
            onValueChange = { onAction(RegistrationAction.EmailChanged(it)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.email),
                    stringResource(R.string.accessibility_email_icon)
                )
            },
            placeholder = { Text(stringResource(R.string.email_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistrationPreview() {
    RegistrationScreen(RegistrationUiState()) {}
}