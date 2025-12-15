package eti.lucasgomes.makalu.features.auth.registration.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import eti.lucasgomes.makalu.components.CameraPermissionDeniedDialog
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.buttons.ExpressiveLoadingButton
import eti.lucasgomes.makalu.components.pickers.SelectOrCaptureImagePicker
import eti.lucasgomes.makalu.features.auth.R
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationAction
import eti.lucasgomes.makalu.features.auth.registration.model.RegistrationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RegistrationScreen(
    uiState: RegistrationUiState,
    onAction: (RegistrationAction) -> Unit
) {
    val keyBoardController = LocalSoftwareKeyboardController.current

    ConfigureTopBar(
        title = stringResource(R.string.registration),
        navigationActions = listOf(),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            val profilePicturePainter by rememberAsyncImagePainter(uiState.profileImage).state.collectAsStateWithLifecycle()
            Image(
                painter = if (uiState.isProfileImageLoaded) profilePicturePainter.painter
                    ?: painterResource(
                        R.drawable.person,
                    ) else painterResource(
                    R.drawable.person
                ),
                contentDescription = stringResource(R.string.accessibility_profile_picture),
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(colorScheme.secondaryContainer)
                    .clickable(
                        enabled = uiState.isLoading.not(),
                        role = Role.Image,
                        onClick = { onAction(RegistrationAction.ProfileImageClicked) })
            )
        }
        TextField(
            label = { Text(stringResource(R.string.name)) },
            enabled = uiState.isLoading.not(),
            modifier = Modifier.fillMaxWidth(),
            value = uiState.name,
            onValueChange = { onAction(RegistrationAction.NameChanged(it)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.badge),
                    stringResource(R.string.accessibility_name_icon)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            ),
        )
        TextField(
            label = { Text(stringResource(R.string.email)) },
            enabled = uiState.isLoading.not(),
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
        TextField(
            label = { Text(stringResource(R.string.phone_number)) },
            enabled = uiState.isLoading.not(),
            modifier = Modifier.fillMaxWidth(),
            value = uiState.phoneNumber,
            onValueChange = { onAction(RegistrationAction.PhoneNumberChanged(it)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.contact_phone),
                    stringResource(R.string.accessibility_phone_number_icon)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )
        TextField(
            label = { Text(stringResource(R.string.password)) },
            enabled = uiState.isLoading.not(),
            modifier = Modifier.fillMaxWidth(),
            value = uiState.password,
            onValueChange = { onAction(RegistrationAction.PasswordChanged(it)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.password),
                    stringResource(R.string.accessibility_password_icon)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            visualTransformation = if (uiState.isPasswordVisible.not()) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { onAction(RegistrationAction.ShowPasswordClicked) }) {
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
        TextField(
            label = { Text(stringResource(R.string.confirm_password)) },
            enabled = uiState.isLoading.not(),
            modifier = Modifier.fillMaxWidth(),
            value = uiState.passwordConfirmation,
            onValueChange = { onAction(RegistrationAction.PasswordConfirmationChanged(it)) },
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
            visualTransformation = if (uiState.isPasswordVisible.not()) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { onAction(RegistrationAction.ShowPasswordClicked) }) {
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
            },
            keyboardActions = KeyboardActions(onDone = {
                keyBoardController?.hide()
                onAction(RegistrationAction.CreateAccountClicked)
            }),
        )
        Spacer(Modifier.weight(1f))
        ExpressiveLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(RegistrationAction.CreateAccountClicked) },
            isLoading = uiState.isLoading
        ) { Text("Create account") }
    }
    if (uiState.isImagePickerVisible) {
        SelectOrCaptureImagePicker(
            onGalleryImageObtained = {
                onAction(RegistrationAction.GalleryImageObtained(it))
            },
            onCameraImageTaken = {
                onAction(RegistrationAction.CameraImageTaken(it))

            },
            onPermissionLauncherResultReceived = { permissionGranted, launchCamera ->
                onAction(
                    RegistrationAction.PermissionLauncherResultReceived(
                        isGranted = permissionGranted,
                        launchCamera = { uri ->
                            launchCamera(uri)
                        })
                )
            },
            onDismissRequest = { onAction(RegistrationAction.ImagePickerDismissed) }
        )
    }
    if (uiState.isPermissionDeniedDialogVisible) {
        CameraPermissionDeniedDialog(
            onDismissRequest = { onAction(RegistrationAction.PermissionDeniedDialogDismissed) },
            onGoToSystemSettings = {
                onAction(
                    RegistrationAction.GoToSystemSettingsClicked
                )
            })
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistrationPreview() {
    RegistrationScreen(RegistrationUiState()) {}
}