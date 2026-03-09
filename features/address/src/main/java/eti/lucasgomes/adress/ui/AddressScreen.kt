package eti.lucasgomes.adress.ui

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import eti.lucasgomes.adress.ui.model.AddressAction
import eti.lucasgomes.adress.ui.model.AddressUiState
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.buttons.ExpressiveLoadingButton
import eti.lucasgomes.makalu.components.dsl.UiText
import eti.lucasgomes.makalu.features.adress.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("MissingPermission")
@Composable
internal fun AddressScreen(uiState: AddressUiState, onAction: (AddressAction) -> Unit) {

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            onAction(AddressAction.GetCurrentLocationClicked(permissionGranted))
        }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(uiState.location) {
        cameraPositionState.position =
            CameraPosition.fromLatLngZoom(uiState.location, 17f)
    }

    ConfigureTopBar(stringResource(R.string.delivery_address))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(visible = uiState.isLocationProvided) {
            Text("Tap do get current location")
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = uiState.isLocationProvided) { showMap ->
                    if (showMap) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(),
                            uiSettings = MapUiSettings(
                                compassEnabled = false,
                                indoorLevelPickerEnabled = false,
                                mapToolbarEnabled = false,
                                myLocationButtonEnabled = false,
                                rotationGesturesEnabled = false,
                                scrollGesturesEnabled = false,
                                scrollGesturesEnabledDuringRotateOrZoom = false,
                                tiltGesturesEnabled = false,
                                zoomControlsEnabled = false,
                                zoomGesturesEnabled = false,
                            )
                        ) {
                            Marker(state = uiState.marker)
                        }
                    } else {
                        Text("Tap do get current location")
                    }
                }
                val overlayColor by animateColorAsState(
                    if (uiState.isLocationLoading) Color.Black.copy(
                        alpha = .2f
                    ) else Color.Transparent
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .background(overlayColor)
                        .clickable(
                            enabled = uiState.isLoading.not() && uiState.isLocationLoading.not(),
                            role = Role.Button,
                            onClick = {
                                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            })
                )
                if (uiState.isLocationLoading) {
                    LoadingIndicator()
                }
            }
        }
        AnimatedVisibility(visible = uiState.locationError != UiText.Empty) {
            Text(uiState.locationError.asString(), color = colorScheme.error)
        }
        TextField(
            label = { Text("Zip code") },
            modifier = Modifier.fillMaxWidth(),
            value = uiState.zipCode.text,
            onValueChange = { onAction(AddressAction.ZipCodeChanged(it)) },
            enabled = uiState.isLoading.not(),
            isError = uiState.zipCode.hasError,
            supportingText = if (uiState.zipCode.hasError) {
                { Text(uiState.zipCode.error.asString()) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            )
        )
        TextField(
            label = { Text("Street") },
            modifier = Modifier.fillMaxWidth(),
            value = uiState.street.text,
            onValueChange = { onAction(AddressAction.StreetChanged(it)) },
            enabled = uiState.isLoading.not(),
            isError = uiState.street.hasError,
            supportingText = if (uiState.street.hasError) {
                { Text(uiState.street.error.asString()) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            )
        )
        TextField(
            label = { Text("Number") },
            modifier = Modifier.fillMaxWidth(),
            value = uiState.number.text,
            onValueChange = { onAction(AddressAction.NumberChanged(it)) },
            enabled = uiState.isLoading.not(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            )
        )
        TextField(
            label = { Text("Complement") },
            modifier = Modifier.fillMaxWidth(),
            value = uiState.complement.text,
            onValueChange = { onAction(AddressAction.ComplementChanged(it)) },
            enabled = uiState.isLoading.not(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(onDone = { onAction(AddressAction.SaveAddressClicked) })
        )
        ExpressiveLoadingButton(
            isLoading = uiState.isLoading,
            onClick = { onAction(AddressAction.SaveAddressClicked) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save address")
        }
    }
}