package eti.lucasgomes.adress.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eti.lucasgomes.adress.ui.model.AddressAction
import eti.lucasgomes.adress.ui.model.AddressUiState
import eti.lucasgomes.makalu.components.appBars.ConfigureTopBar
import eti.lucasgomes.makalu.components.buttons.ExpressiveLoadingButton
import eti.lucasgomes.makalu.features.adress.R

@Composable
internal fun AddressScreen(uiState: AddressUiState, onAction: (AddressAction) -> Unit) {
    ConfigureTopBar(stringResource(R.string.delivery_address))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Tap do add a location")
            }
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