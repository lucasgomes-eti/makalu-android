package eti.lucasgomes.adress.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
            value = uiState.zipCode,
            onValueChange = { onAction(AddressAction.ZipCodeChanged(it)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            )
        )
        TextField(
            label = { Text("Street") },
            modifier = Modifier.fillMaxWidth(),
            value = uiState.street,
            onValueChange = { onAction(AddressAction.StreetChanged(it)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            )
        )
        TextField(
            label = { Text("Number") },
            modifier = Modifier.fillMaxWidth(),
            value = uiState.number,
            onValueChange = { onAction(AddressAction.NumberChanged(it)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            )
        )
        TextField(
            label = { Text("Complement") },
            modifier = Modifier.fillMaxWidth(),
            value = uiState.complement,
            onValueChange = { onAction(AddressAction.ComplementChanged(it)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Words
            )
        )
    }
}