package eti.lucasgomes.makalu.features.profile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import eti.lucasgomes.makalu.components.CameraPermissionDeniedDialog
import eti.lucasgomes.makalu.components.banners.ErrorBanner
import eti.lucasgomes.makalu.components.dsl.OnFirstComposition
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.components.pickers.SelectOrCaptureImagePicker
import eti.lucasgomes.makalu.features.profile.R
import eti.lucasgomes.makalu.features.profile.model.ProfileAction
import eti.lucasgomes.makalu.features.profile.model.ProfileUiState

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
        AnimatedVisibility(uiState.generalError != UiText.Empty) {
            ErrorBanner(uiState.generalError.asString()) { onAction(ProfileAction.ErrorDismiss) }
        }
        Crossfade(uiState.isLoading) { isLoading ->
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    LoadingIndicator()
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        val profilePicturePainter by rememberAsyncImagePainter(uiState.imageUrlString).state.collectAsStateWithLifecycle()
                        Image(
                            painter = if (uiState.isProfileImageLoaded) profilePicturePainter.painter
                                ?: painterResource(
                                    eti.lucasgomes.makalu.components.R.drawable.person,
                                ) else painterResource(
                                eti.lucasgomes.makalu.components.R.drawable.person
                            ),
                            contentDescription = stringResource(eti.lucasgomes.makalu.components.R.string.accessibility_profile_picture),
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(colorScheme.secondaryContainer)
                                .clickable(
                                    enabled = uiState.isLoading.not(),
                                    role = Role.Image,
                                    onClick = { onAction(ProfileAction.ProfileImageClicked) })
                        )
                        if (uiState.imageUploadLoading) {
                            LoadingIndicator()
                        }
                    }
                    Text(uiState.name, style = typography.titleLarge)
                    Column {
                        Text(
                            stringResource(eti.lucasgomes.makalu.components.R.string.email),
                            modifier = Modifier.fillMaxWidth(),
                            style = typography.labelSmall
                        )
                        Text(
                            uiState.email,
                            modifier = Modifier.fillMaxWidth(),
                            style = typography.bodyLarge
                        )
                    }
                    Column {
                        Text(
                            stringResource(eti.lucasgomes.makalu.components.R.string.phone_number),
                            modifier = Modifier.fillMaxWidth(),
                            style = typography.labelSmall
                        )
                        Text(
                            uiState.phoneNumber,
                            modifier = Modifier.fillMaxWidth(),
                            style = typography.bodyLarge
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
        ListItem(
            headlineContent = { Text(stringResource(R.string.logout)) },
            leadingContent = {
                Icon(
                    painterResource(R.drawable.exit_to_app),
                    contentDescription = null,
                    tint = colorScheme.onSurface
                )
            },
            modifier = Modifier.Companion.clickable(
                role = Role.Companion.Button,
                onClick = { onAction(ProfileAction.LogoutClicked) }
            )
        )
    }

    if (uiState.isLogoutDialogVisible) {
        LogoutDoubleCheckDialog(
            onDismissRequest = { onAction(ProfileAction.LogoutDialogDismissed) },
            onLogout = { onAction(ProfileAction.LogoutConfirmed) })
    }

    if (uiState.isImagePickerVisible) {
        SelectOrCaptureImagePicker(
            onGalleryImageObtained = {
                onAction(ProfileAction.GalleryImageObtained(it))
            },
            onCameraImageTaken = {
                onAction(ProfileAction.CameraImageTaken(it))

            },
            onPermissionLauncherResultReceived = { permissionGranted, launchCamera ->
                onAction(
                    ProfileAction.PermissionLauncherResultReceived(
                        isGranted = permissionGranted,
                        launchCamera = { uri ->
                            launchCamera(uri)
                        })
                )
            },
            onDismissRequest = { onAction(ProfileAction.ImagePickerDismissed) }
        )
    }
    if (uiState.isPermissionDeniedDialogVisible) {
        CameraPermissionDeniedDialog(
            onDismissRequest = { onAction(ProfileAction.PermissionDeniedDialogDismissed) },
            onGoToSystemSettings = {
                onAction(
                    ProfileAction.GoToSystemSettingsClicked
                )
            })
    }
}

@Composable
private fun LogoutDoubleCheckDialog(onDismissRequest: () -> Unit, onLogout: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(
                painterResource(R.drawable.exit_to_app),
                null
            )
        },
        title = { Text(stringResource(R.string.logout_double_check_header)) },
        text = { Text(stringResource(R.string.logout_double_check_confirmation)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onLogout) {
                Text(stringResource(R.string.logout))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.onSurface)
            ) {
                Text(stringResource(eti.lucasgomes.makalu.components.R.string.dismiss))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    ProfileScreen(
        ProfileUiState(
            name = "Makalu User",
            email = "email@makalu.com",
            phoneNumber = "99999999999"
        )
    ) {}
}