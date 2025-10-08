package eti.lucasgomes.makalu.features.auth.login.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eti.lucasgomes.makalu.components.buttons.ExpressiveLoadingButton
import eti.lucasgomes.makalu.components.buttons.ExpressiveTextButton
import eti.lucasgomes.makalu.features.auth.R
import eti.lucasgomes.makalu.features.auth.login.model.LoginAction
import eti.lucasgomes.makalu.features.auth.login.model.LoginUiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoginScreen(uiState: LoginUiState, onAction: (LoginAction) -> Unit) {

    val keyBoardController = LocalSoftwareKeyboardController.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(128.dp),
            painter = painterResource(R.drawable.storefront),
            contentDescription = stringResource(R.string.accessibility_makalu_logo),
            tint = colorScheme.primary
        )
        Text("Makalu", style = typography.displayLarge)
        TextField(
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isLoading.not(),
            value = uiState.email,
            onValueChange = { onAction(LoginAction.EmailChanged(it)) },
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
        TextField(
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isLoading.not(),
            value = uiState.password,
            onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.password),
                    stringResource(R.string.accessibility_password_icon)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyBoardController?.hide()
                onAction(LoginAction.LoginClicked)
            }),
            visualTransformation = if (uiState.isPasswordVisible.not()) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { onAction(LoginAction.ShowPasswordClicked) }) {
                    Crossfade(uiState.isPasswordVisible) { isPasswordVisible ->
                        if (isPasswordVisible) {
                            Icon(
                                painterResource(R.drawable.visibility_off),
                                stringResource(R.string.accessibility_hide_password_icon)
                            )
                        } else {
                            Icon(
                                painterResource(R.drawable.visibility),
                                stringResource(R.string.accessibility_show_password_icon)
                            )

                        }
                    }
                }
            }
        )
        ExpressiveLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(LoginAction.LoginClicked) },
            isLoading = uiState.isLoading
        ) { Text(stringResource(R.string.login)) }
        Spacer(Modifier.height(64.dp))
        Text(stringResource(R.string.don_t_have_an_account_yet), style = typography.labelLarge)
        ExpressiveTextButton(
            onClick = { onAction(LoginAction.RegistrationClicked) },
            enabled = uiState.isLoading.not()
        ) { Text(stringResource(R.string.create_an_account)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    LoginScreen(LoginUiState()) {}
}